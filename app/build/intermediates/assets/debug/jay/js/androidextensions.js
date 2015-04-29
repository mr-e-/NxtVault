/**
 * Created with IntelliJ IDEA.
 * User: Brandon
 * Date: 4/13/15
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */

var AndroidExtensions = {
    reviewData: [],

    getBestNodes: function(){
        Jay.nodeScan(function(){
            MyInterface.getBestNodesResult(JSON.stringify(Jay.bestNodes));
        });
    },

    startTRF: function(sender, trfBytes){
        var bytes = base62Decode(trfBytes.substring(3));
        console.log(JSON.stringify(bytes));
        if(bytes[0] == '1')
        {
            bytes = bytes.slice(1);
            if(bytes.length == 31) bytes = bytes.slice(0, 30);

            var collect = [];
            collect = [bytes[0],bytes[1]]; // type ver & subtype
            collect = collect.concat(nxtTimeBytes()); // timestamp
            collect = collect.concat(wordBytes(1440)); // deadline
            var senderPubKey = converters.hexStringToByteArray(findAccount(sender).publicKey);
            collect = collect.concat(senderPubKey);
            collect = collect.concat(bytes.slice(2, 2+8)); // recipient/genesis
            collect = collect.concat(bytes.slice(10, 10+8)); // amount
            collect = collect.concat(bytes.slice(18, 18+8)); // fee
            collect = collect.concat(pad(32, 0)); // reftxhash
            collect = collect.concat(pad(64, 0)); // signature bytes
            collect = collect.concat(bytes.slice(26, 26+4)); // flags
            collect = collect.concat(pad(4, 0)); // EC blockheight
            collect = collect.concat(pad(8, 0)); // EC blockid
            if(bytes.length > 30) collect = collect.concat(bytes.slice(30)); // attachment/appendages

            return collect;
        }
    },

    signTrfBytes: function(sender, trfBytes, secretPhrase){
        var bytes = this.startTRF(sender, trfBytes);

        var sig = signBytes(bytes, secretPhrase);
        var signed = bytes.slice(0,96);
        signed = signed.concat(sig);
        signed = signed.concat(bytes.slice(96+64));

        return converters.byteArrayToHexString(signed);
    },

    //Needed to override so I could store accountName
    storeAccount: function(account){
        var sto = [];
        if(localStorage["accounts"])
        {
            sto = JSON.parse(localStorage["accounts"]);
        }

        var acc = {};
        var existing = false;
        for (var i = 0; i < sto.length; i++){
            if (sto[i]["accountRS"] === account["accountRS"]){
                acc = sto[i];
                existing = true;
                break;
            }
        }

        acc["accountRS"] = account["accountRS"];
        acc["publicKey"] = account["publicKey"];
        acc["cipher"] = account["cipher"];
        acc["checksum"] = account["checksum"];
        acc["accountName"] = account["accountName"];

        if (!existing)
            sto.push(acc);

        localStorage["accounts"] = JSON.stringify(sto);
    },
    deleteAccount: function (address){
        var data = localStorage["accounts"];
        var accounts = JSON.parse(localStorage["accounts"]);

        for(var a=0;a<accounts.length;a++)
        {
            if(accounts[a]["accountRS"] == address)
            {
                accounts.splice(a, 1);
            }
        }
        localStorage["accounts"] = JSON.stringify(accounts);
    },
    setReview: function(number, key, value){
        this.reviewData.push({id: number, key: key, value:value});
    },
    extractBytesData: function(sender, trfBytes)
    {
        // lets think here.
        // first we take out the version and subversion, and then think from there
        // have about 8 different places to put data, then account for all possible types
        // appendages will have dropdowns with their content and won't take up much room.
        // the 8 zones will need to be really small.
        // type sender amount recip extra for attachment...

        this.reviewData = [];

        var bytes = this.startTRF(sender, trfBytes);
        
        var type = bytes[0];
        var subtype = bytes[1] % 16;
        var sender = getAccountIdFromPublicKey(converters.byteArrayToHexString(bytes.slice(8, 8+32)), true);
        var r = new NxtAddress();
        r.set(byteArrayToBigInteger(bytes.slice(40, 48)).toString());
        var recipient = r.toString();
        var amount = byteArrayToBigInteger(bytes.slice(48, 48+8));
        var fee = byteArrayToBigInteger(bytes.slice(56, 56+8));
        var flags = converters.byteArrayToSignedInt32(bytes.slice(160, 160+4));
        rest = [];
        if(bytes.length > 176) rest = bytes.slice(176);
        var msg = [];
        if(type == 0)
        {
            if(subtype == 0)
            {
                typeName = "Ordinary Payment";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Sender", sender);
                this.setReview(3, "Recipient", recipient);
                this.setReview(4, "Amount", amount/100000000 + " nxt");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length) msg = rest;
            }
        }
        else if(type == 1)
        {
            if(subtype == 0)
            {
                typeName = "Arbitrary Message";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Sender", sender);
                this.setReview(3, "Recipient", recipient);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                if(rest.length) msg = rest;
            }
            else if(subtype == 1)
            {
                typeName = "Alias Assignment";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Registrar", sender);
                var alias = converters.byteArrayToString(rest.slice(2, rest[1]+2));
                this.setReview(3, "Alias Name", alias);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                var data = converters.byteArrayToString(rest.slice(4+rest[1], 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]])));
                if(rest.length > 2+rest[1]+bytesWord(rest.slice(2+rest[1], 4+rest[1]))) msg = rest.slice(2+rest[1]+bytesWord(rest.slice(2+rest[1], 4+rest[1])));
            }
            else if(subtype == 2)
            {
                typeName = "Poll Creation"; //  not yet
            }
            else if(subtype == 3)
            {
                typeName = "Vote Casting"; // not yet
            }
            else if(subtype == 4)
            {
                typeName = "Hub Announcement"; //  what even is this?
            }
            else if(subtype == 5)
            {
                typeName = "Account Info";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Account", sender);
                var alias = converters.byteArrayToString(rest.slice(2, rest[1]+2));
                this.setReview(3, "Name", alias);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                var data = converters.byteArrayToString(rest.slice(4+rest[1], 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]])));
                if(rest.length > 2+rest[1]+bytesWord(rest.slice(2+rest[1], 4+rest[1]))) msg = rest.slice(2+rest[1]+bytesWord(rest.slice(2+rest[1], 4+rest[1])));
            }
            else if(subtype == 6)
            {
                typeName = "Alias Sell";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var alias = converters.byteArrayToString(rest.slice(2, rest[1]+2));
                if(recipient == "NXT-2222-2222-2222-22222") this.setReview(3, "Buyer", "Anyone");
                else this.setReview(3, "Buyer", recipient);
                this.setReview(4, "Alias Name", alias);
                var price = byteArrayToBigInteger(rest.slice(2+rest[1], 10+rest[1])).toString();
                this.setReview(5, "Sell Price", price);
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 10+rest[1]) msg = rest.slice(10+rest[1]);
            }
            else if(subtype == 7)
            {
                typeName = "Alias Buy";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Buyer", sender);
                this.setReview(3, "Seller", recipient);
                var alias = converters.byteArrayToString(rest.slice(2, rest[1]+2));
                this.setReview(4, "Alias", alias);
                this.setReview(5, "Buy Price", amount/100000000 + " nxt");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 2+rest[1]) msg = rest.slice(2+rest[1])
            }
        }
        else if(type == 2)
        {
            if(subtype == 0)
            {
                typeName = "Asset Issuance";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Issuer", sender);
                var name = converters.byteArrayToString(rest.slice(2,rest[1]+2));
                this.setReview(3, "Asset Name", name);
                var data = converters.byteArrayToString(rest.slice(4+rest[1], 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]])));
                var newpos = 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]]);
                var units = byteArrayToBigInteger(rest.slice(newpos, newpos+8));
                this.setReview(4, "Units", units);
                this.setReview(5, "Decimals", rest[newpos+8]);
                this.setReview(6, "Fee", fee/100000000 + " nxt");
            }
            else if(subtype == 1)
            {
                typeName = "Asset Transfer";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Sender", sender);
                this.setReview(3, "Recipient", recipient);
                var assetId = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(4, "Asset Id", assetId);
                var amount = byteArrayToBigInteger(rest.slice(1+8, 1+16)).toString();
                this.setReview(5, "Amount", amount/100000000);
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 17) msg = rest.slice(17);
            }
            else if(subtype == 2)
            {
                typeName = "Ask Order Placement";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Trader", sender);
                var assetId = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Asset Id", assetId);
                var amount = byteArrayToBigInteger(rest.slice(1+8, 1+16)).toString();
                this.setReview(4, "Amount", amount/100000000);
                var price = byteArrayToBigInteger(rest.slice(1+16, 1+24)).toString();
                this.setReview(5, "Price", price/100000000 + " nxt");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 25) msg = rest.slice(25);
            }
            else if(subtype == 3)
            {
                typeName = "Bid Order Placement";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Trader", sender);
                var assetId = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Asset Id", assetId);
                var amount = byteArrayToBigInteger(rest.slice(1+8, 1+16)).toString();
                this.setReview(4, "Amount", amount/100000000);
                var price = byteArrayToBigInteger(rest.slice(1+16, 1+24)).toString();
                this.setReview(5, "Price", price/100000000 + " nxt");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 25) msg = rest.slice(25);
            }
            else if(subtype == 4)
            {
                typeName = "Ask Order Cancellation";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Trader", sender);
                var order = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Order Id", order);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                if(rest.length > 9) msg = rest.slice(9);
            }
            else if(subtype == 5)
            {
                typeName = "Bid Order Cancellation";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Trader", sender);
                var order = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Order Id", order);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                if(rest.length > 9) msg = rest.slice(9);
            }
        }
        else if(type == 3)
        {
            if(subtype == 0)
            {
                typeName = "Goods Listing";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var name = converters.byteArrayToString(rest.slice(3,rest[1]+2));
                this.setReview(3, "Good Name", name);
                var data = converters.byteArrayToString(rest.slice(4+rest[1], 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]])));
                var newpos = 4+rest[1]+bytesWord([rest[2+rest[1]], rest[3+rest[1]]]);
                var tags = converters.byteArrayToString(rest.slice(newpos+2, newpos+2+bytesWord([rest[newpos],rest[newpos+1]])));
                newpos = newpos+2+bytesWord([rest[newpos],rest[newpos+1]]);
                this.setReview(4, "Tags", tags);
                var amount = converters.byteArrayToSignedInt32(rest.slice(newpos, newpos+4));
                var price = byteArrayToBigInteger(rest.slice(newpos+4, newpos+12)).toString();
                this.setReview(5, "Amount (price)", amount + "(" + price/100000000 + " nxt)");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
            }
            else if(subtype == 1)
            {
                typeName = "Goods Delisting";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var order = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Item Id", order);
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                if(rest.length > 9) msg = rest.slice(9);
    
            }
            else if(subtype == 2)
            {
                typeName = "Price Change";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Item Id", goodid);
                var newprice = byteArrayToBigInteger(rest.slice(1+8, 1+8+8)).toString();
                this.setReview(4, "New Price", nowprice/100000000 + " nxt");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+8+8) msg = rest.slice(17);
            }
            else if(subtype == 3)
            {
                typeName = "Quantity Change";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Item Id", goodid);
                var chg = converters.byteArrayToSignedInt32(rest.slice(1+8, 1+8+4));
                if(chg < 0) this.setReview(4, "Decrease By", -chg);
                else this.setReview(4, "Increase By", chg);
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+8+4) msg = rest.slice(13);
            }
            else if(subtype == 4)
            {
                typeName = "Purchase";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Buyer", sender);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Item Id", goodid);
                var qnt = byteArrayToBigInteger(rest.slice(1+8, 1+8+4)).toString();
                this.setReview(4, "Quantity", qnt);
                var price = byteArrayToBigInteger(rest.slice(1+8+4, 1+16+4)).toString();
                this.setReview(5, "Price", price/100000000 + " nxt");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+16+8) msg = rest.slice(25);
            }
            else if(subtype == 5)
            {
                typeName = "Delivery";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Item Id", goodid);
                var discount = byteArrayToBigInteger(rest.slice(rest.length-8)).toString();
                this.setReview(4, "Discount", discount/100000000 + " nxt");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+8) msg = rest.slice(9);
    
            }
            else if(subtype == 6)
            {
                typeName = "Feedback";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "User", sender);
                this.setReview(3, "Seller", recipient);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(4, "Item Id", goodid);
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+8) msg = rest.slice(9);
            }
            else if(subtype == 7)
            {
                typeName = "Refund";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Seller", sender);
                var goodid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Purchase Id", goodid);
                var discount = byteArrayToBigInteger(rest.slice(1+8,1+16)).toString();
                this.setReview(4, "Refund Amount", discount/100000000 + " nxt");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 1+16) msg = rest.slice(17);
            }
        }
        else if(type == 4)
        {
            if(subtype == 0)
            {
                typeName = "Balance Leasing";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Lessor", sender);
                var lease = bytesWord(rest.slice(1,3));
                this.setReview(3, "Length", lease + " blocks");
                this.setReview(4, "Fee", fee/100000000 + " nxt");
                if(rest.length > 3) msg = rest.slice(3);
            }
        }
        else if(type == 5)
        {
            if(subtype == 0)
            {
                typeName = "Issue Currency";
            }
            else if(subtype == 1)
            {
                typeName = "Reserve Increase";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Reserver", sender);
                var assetid = converters.byteArrayToString(rest.slice(1, 1+8));
                this.setReview(3, "Currency Id", assetId);
                var amount = byteArrayToBigInteger(rest.slice(1+8, 1+16)).toString();
                this.setReview(4, "Amount per Unit", amount + " nxt");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 17) msg = rest.slice(17);
            }
            else if(subtype == 2)
            {
                typeName = "Reserve Claim";
            }
            else if(subtype == 3)
            {
                typeName = "Currency Transfer";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Sender", sender);
                this.setReview(3, "Recipient", recipient);
                var ms = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(4, "Currency Id", ms);
                var amount = byteArrayToBigInteger(rest.slice(1+8, 1+16)).toString();
                this.setReview(5, "Amount", amount + " QNT");
                this.setReview(6, "Fee", fee/100000000 + " nxt");
                if(rest.length > 17) msg = rest.slice(17);
            }
            else if(subtype == 4)
            {
                typeName = "Exchange Offer";
            }
            else if(subtype == 5)
            {
                typeName = "Exchange Buy";
            }
            else if(subtype == 6)
            {
                typeName = "Exchange Sell";
            }
            else if(subtype == 7)
            {
                typeName = "Mint Currency";
                this.setReview(1, "Type", typeName);
                this.setReview(2, "Minter", sender);
                var assetid = byteArrayToBigInteger(rest.slice(1, 1+8)).toString();
                this.setReview(3, "Currency Id", assetId);
                var amount = byteArrayToBigInteger(rest.slice(1+16, 1+24)).toString();
                this.setReview(4, "Amount To Mint", amount + " Units");
                this.setReview(5, "Fee", fee/100000000 + " nxt");
                if(rest.length > 16+16+1) msg = rest.slice(33);
            }
            else if(subtype == 8)
            {
                typeName = "Delete Currency";
            }
        }
    
        var message = getModifierBit(flags, 0);
        var publicKey = getModifierBit(flags, 2);
        if(message && msg.length)
        {
            var len = bytesWord([msg[1],msg[2]]);
            var str = converters.byteArrayToString(msg.slice(5,5+len));
            msg = msg.slice(3+len);
        }
        if(publicKey && msg.length)
        {
            var str = converters.byteArrayToHexString(msg.slice(1,65));
            msg = msg.slice(65);
        }

        return this.reviewData;
    },
    onRequestSuccess: function(data){
        MyInterface.onRequestSuccess(data);
    },
    onRequestFailed: function(message){
        MyInterface.onRequestFailed();
    }
};
