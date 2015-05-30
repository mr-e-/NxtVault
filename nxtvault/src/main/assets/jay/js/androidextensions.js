/**
 * Created with IntelliJ IDEA.
 * User: Brandon
 * Date: 4/13/15
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */

var AndroidExtensions = {
    reviewData: [],

	storePin: function(pin){
		var pinData = {};

		pinData["cypher"] = encryptSecretPhrase("pin", pin).toString();
		pinData["checksum"] = converters.byteArrayToHexString(simpleHash(converters.stringToByteArray(pin)));

		localStorage["pin"] = JSON.stringify(pinData);
	},

	verifyPin: function(pin){
		var result = false;

		if (localStorage["pin"]){
			var pinData = JSON.parse(localStorage["pin"]);
			var cypher = encryptSecretPhrase("pin", pin).toString();
			var checksum = converters.byteArrayToHexString(simpleHash(converters.stringToByteArray(pin)));

			if (pinData["cypher"] === cypher && pinData["checksum"] == checksum){
				result = true;
			}
		}

		return result;
	},

    getBestNodes: function(){
        Jay.nodeScan(function(){
            MyInterface.getBestNodesResult(JSON.stringify(Jay.bestNodes));
        });
    },

    getAccounts: function(){
        var accounts = localStorage['accounts'];
        if (accounts == undefined){
            return "";
        }
        else{
            return JSON.stringify(JSON.parse(accounts));
        }
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
        var bytes;

        if (trfBytes.indexOf("TX_") > -1){
            bytes = this.startTRF(sender, trfBytes);
        }
        else{
            bytes = trfBytes;
        }

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
    changePin: function(oldpin, pin){
        var accounts = JSON.parse(localStorage["accounts"]);

        for(var a=0;a<accounts.length;a++)
        {
            // now lets handle...
            var sec = decryptSecretPhrase(accounts[a]["cipher"], oldpin, accounts[a]["checksum"]).toString();
            var newcipher = encryptSecretPhrase(sec, pin).toString();
            accounts[a]["cipher"] = newcipher;
        }
        localStorage["accounts"] = JSON.stringify(accounts);
        return true;
    },
    setReview: function(number, key, value){
        this.reviewData.push({id: number, key: key, value:value});
    },
    extractBytesData: function(sender, trfBytes)
    {
            this.reviewData = [];
            var bytes;

            if (trfBytes.indexOf("TX_") > -1){
                bytes = this.startTRF(sender, trfBytes);
            }
            else{
                bytes = trfBytes;
            }

            // lets think here.
        	// first we take out the version and subversion, and then think from there
        	// have about 8 different places to put data, then account for all possible types
        	// appendages will have dropdowns with their content and won't take up much room.
        	// the 8 zones will need to be really small.
        	// type sender amount recip extra for attachment...

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
        			this.setReview(5, "Amount", amount + " QNT");
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
        			this.setReview(4, "Amount", amount + " QNT");
        			var price = byteArrayToBigInteger(rest.slice(1+16, 1+24)).toString();
        			this.setReview(5, "Price", price + " NQT");
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
        			this.setReview(4, "Amount", amount + " QNT");
        			var price = byteArrayToBigInteger(rest.slice(1+16, 1+24)).toString();
        			this.setReview(5, "Price", price + " NQT");
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
        			setRevieW(2, "Seller", sender);
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
        MyInterface.onRequestFailed(message);
    },
    generatePassPhrase: function() {
        var passPhrase = "";

    	var wordCount = 1626;

    	var allWords = ["like", "just", "love", "know", "never", "want", "time", "out", "there", "make", "look", "eye", "down", "only", "think", "heart", "back", "then", "into", "about", "more", "away", "still", "them", "take", "thing", "even", "through", "long", "always", "world", "too", "friend", "tell", "try", "hand", "thought", "over", "here", "other", "need", "smile", "again", "much", "cry", "been", "night", "ever", "little", "said", "end", "some", "those", "around", "mind", "people", "girl", "leave", "dream", "left", "turn", "myself", "give", "nothing", "really", "off", "before", "something", "find", "walk", "wish", "good", "once", "place", "ask", "stop", "keep", "watch", "seem", "everything", "wait", "got", "yet", "made", "remember", "start", "alone", "run", "hope", "maybe", "believe", "body", "hate", "after", "close", "talk", "stand", "own", "each", "hurt", "help", "home", "god", "soul", "new", "many", "two", "inside", "should", "true", "first", "fear", "mean", "better", "play", "another", "gone", "change", "use", "wonder", "someone", "hair", "cold", "open", "best", "any", "behind", "happen", "water", "dark", "laugh", "stay", "forever", "name", "work", "show", "sky", "break", "came", "deep", "door", "put", "black", "together", "upon", "happy", "such", "great", "white", "matter", "fill", "past", "please", "burn", "cause", "enough", "touch", "moment", "soon", "voice", "scream", "anything", "stare", "sound", "red", "everyone", "hide", "kiss", "truth", "death", "beautiful", "mine", "blood", "broken", "very", "pass", "next", "forget", "tree", "wrong", "air", "mother", "understand", "lip", "hit", "wall", "memory", "sleep", "free", "high", "realize", "school", "might", "skin", "sweet", "perfect", "blue", "kill", "breath", "dance", "against", "fly", "between", "grow", "strong", "under", "listen", "bring", "sometimes", "speak", "pull", "person", "become", "family", "begin", "ground", "real", "small", "father", "sure", "feet", "rest", "young", "finally", "land", "across", "today", "different", "guy", "line", "fire", "reason", "reach", "second", "slowly", "write", "eat", "smell", "mouth", "step", "learn", "three", "floor", "promise", "breathe", "darkness", "push", "earth", "guess", "save", "song", "above", "along", "both", "color", "house", "almost", "sorry", "anymore", "brother", "okay", "dear", "game", "fade", "already", "apart", "warm", "beauty", "heard", "notice", "question", "shine", "began", "piece", "whole", "shadow", "secret", "street", "within", "finger", "point", "morning", "whisper", "child", "moon", "green", "story", "glass", "kid", "silence", "since", "soft", "yourself", "empty", "shall", "angel", "answer", "baby", "bright", "dad", "path", "worry", "hour", "drop", "follow", "power", "war", "half", "flow", "heaven", "act", "chance", "fact", "least", "tired", "children", "near", "quite", "afraid", "rise", "sea", "taste", "window", "cover", "nice", "trust", "lot", "sad", "cool", "force", "peace", "return", "blind", "easy", "ready", "roll", "rose", "drive", "held", "music", "beneath", "hang", "mom", "paint", "emotion", "quiet", "clear", "cloud", "few", "pretty", "bird", "outside", "paper", "picture", "front", "rock", "simple", "anyone", "meant", "reality", "road", "sense", "waste", "bit", "leaf", "thank", "happiness", "meet", "men", "smoke", "truly", "decide", "self", "age", "book", "form", "alive", "carry", "escape", "damn", "instead", "able", "ice", "minute", "throw", "catch", "leg", "ring", "course", "goodbye", "lead", "poem", "sick", "corner", "desire", "known", "problem", "remind", "shoulder", "suppose", "toward", "wave", "drink", "jump", "woman", "pretend", "sister", "week", "human", "joy", "crack", "grey", "pray", "surprise", "dry", "knee", "less", "search", "bleed", "caught", "clean", "embrace", "future", "king", "son", "sorrow", "chest", "hug", "remain", "sat", "worth", "blow", "daddy", "final", "parent", "tight", "also", "create", "lonely", "safe", "cross", "dress", "evil", "silent", "bone", "fate", "perhaps", "anger", "class", "scar", "snow", "tiny", "tonight", "continue", "control", "dog", "edge", "mirror", "month", "suddenly", "comfort", "given", "loud", "quickly", "gaze", "plan", "rush", "stone", "town", "battle", "ignore", "spirit", "stood", "stupid", "yours", "brown", "build", "dust", "hey", "kept", "pay", "phone", "twist", "although", "ball", "beyond", "hidden", "nose", "taken", "fail", "float", "pure", "somehow", "wash", "wrap", "angry", "cheek", "creature", "forgotten", "heat", "rip", "single", "space", "special", "weak", "whatever", "yell", "anyway", "blame", "job", "choose", "country", "curse", "drift", "echo", "figure", "grew", "laughter", "neck", "suffer", "worse", "yeah", "disappear", "foot", "forward", "knife", "mess", "somewhere", "stomach", "storm", "beg", "idea", "lift", "offer", "breeze", "field", "five", "often", "simply", "stuck", "win", "allow", "confuse", "enjoy", "except", "flower", "seek", "strength", "calm", "grin", "gun", "heavy", "hill", "large", "ocean", "shoe", "sigh", "straight", "summer", "tongue", "accept", "crazy", "everyday", "exist", "grass", "mistake", "sent", "shut", "surround", "table", "ache", "brain", "destroy", "heal", "nature", "shout", "sign", "stain", "choice", "doubt", "glance", "glow", "mountain", "queen", "stranger", "throat", "tomorrow", "city", "either", "fish", "flame", "rather", "shape", "spin", "spread", "ash", "distance", "finish", "image", "imagine", "important", "nobody", "shatter", "warmth", "became", "feed", "flesh", "funny", "lust", "shirt", "trouble", "yellow", "attention", "bare", "bite", "money", "protect", "amaze", "appear", "born", "choke", "completely", "daughter", "fresh", "friendship", "gentle", "probably", "six", "deserve", "expect", "grab", "middle", "nightmare", "river", "thousand", "weight", "worst", "wound", "barely", "bottle", "cream", "regret", "relationship", "stick", "test", "crush", "endless", "fault", "itself", "rule", "spill", "art", "circle", "join", "kick", "mask", "master", "passion", "quick", "raise", "smooth", "unless", "wander", "actually", "broke", "chair", "deal", "favorite", "gift", "note", "number", "sweat", "box", "chill", "clothes", "lady", "mark", "park", "poor", "sadness", "tie", "animal", "belong", "brush", "consume", "dawn", "forest", "innocent", "pen", "pride", "stream", "thick", "clay", "complete", "count", "draw", "faith", "press", "silver", "struggle", "surface", "taught", "teach", "wet", "bless", "chase", "climb", "enter", "letter", "melt", "metal", "movie", "stretch", "swing", "vision", "wife", "beside", "crash", "forgot", "guide", "haunt", "joke", "knock", "plant", "pour", "prove", "reveal", "steal", "stuff", "trip", "wood", "wrist", "bother", "bottom", "crawl", "crowd", "fix", "forgive", "frown", "grace", "loose", "lucky", "party", "release", "surely", "survive", "teacher", "gently", "grip", "speed", "suicide", "travel", "treat", "vein", "written", "cage", "chain", "conversation", "date", "enemy", "however", "interest", "million", "page", "pink", "proud", "sway", "themselves", "winter", "church", "cruel", "cup", "demon", "experience", "freedom", "pair", "pop", "purpose", "respect", "shoot", "softly", "state", "strange", "bar", "birth", "curl", "dirt", "excuse", "lord", "lovely", "monster", "order", "pack", "pants", "pool", "scene", "seven", "shame", "slide", "ugly", "among", "blade", "blonde", "closet", "creek", "deny", "drug", "eternity", "gain", "grade", "handle", "key", "linger", "pale", "prepare", "swallow", "swim", "tremble", "wheel", "won", "cast", "cigarette", "claim", "college", "direction", "dirty", "gather", "ghost", "hundred", "loss", "lung", "orange", "present", "swear", "swirl", "twice", "wild", "bitter", "blanket", "doctor", "everywhere", "flash", "grown", "knowledge", "numb", "pressure", "radio", "repeat", "ruin", "spend", "unknown", "buy", "clock", "devil", "early", "false", "fantasy", "pound", "precious", "refuse", "sheet", "teeth", "welcome", "add", "ahead", "block", "bury", "caress", "content", "depth", "despite", "distant", "marry", "purple", "threw", "whenever", "bomb", "dull", "easily", "grasp", "hospital", "innocence", "normal", "receive", "reply", "rhyme", "shade", "someday", "sword", "toe", "visit", "asleep", "bought", "center", "consider", "flat", "hero", "history", "ink", "insane", "muscle", "mystery", "pocket", "reflection", "shove", "silently", "smart", "soldier", "spot", "stress", "train", "type", "view", "whether", "bus", "energy", "explain", "holy", "hunger", "inch", "magic", "mix", "noise", "nowhere", "prayer", "presence", "shock", "snap", "spider", "study", "thunder", "trail", "admit", "agree", "bag", "bang", "bound", "butterfly", "cute", "exactly", "explode", "familiar", "fold", "further", "pierce", "reflect", "scent", "selfish", "sharp", "sink", "spring", "stumble", "universe", "weep", "women", "wonderful", "action", "ancient", "attempt", "avoid", "birthday", "branch", "chocolate", "core", "depress", "drunk", "especially", "focus", "fruit", "honest", "match", "palm", "perfectly", "pillow", "pity", "poison", "roar", "shift", "slightly", "thump", "truck", "tune", "twenty", "unable", "wipe", "wrote", "coat", "constant", "dinner", "drove", "egg", "eternal", "flight", "flood", "frame", "freak", "gasp", "glad", "hollow", "motion", "peer", "plastic", "root", "screen", "season", "sting", "strike", "team", "unlike", "victim", "volume", "warn", "weird", "attack", "await", "awake", "built", "charm", "crave", "despair", "fought", "grant", "grief", "horse", "limit", "message", "ripple", "sanity", "scatter", "serve", "split", "string", "trick", "annoy", "blur", "boat", "brave", "clearly", "cling", "connect", "fist", "forth", "imagination", "iron", "jock", "judge", "lesson", "milk", "misery", "nail", "naked", "ourselves", "poet", "possible", "princess", "sail", "size", "snake", "society", "stroke", "torture", "toss", "trace", "wise", "bloom", "bullet", "cell", "check", "cost", "darling", "during", "footstep", "fragile", "hallway", "hardly", "horizon", "invisible", "journey", "midnight", "mud", "nod", "pause", "relax", "shiver", "sudden", "value", "youth", "abuse", "admire", "blink", "breast", "bruise", "constantly", "couple", "creep", "curve", "difference", "dumb", "emptiness", "gotta", "honor", "plain", "planet", "recall", "rub", "ship", "slam", "soar", "somebody", "tightly", "weather", "adore", "approach", "bond", "bread", "burst", "candle", "coffee", "cousin", "crime", "desert", "flutter", "frozen", "grand", "heel", "hello", "language", "level", "movement", "pleasure", "powerful", "random", "rhythm", "settle", "silly", "slap", "sort", "spoken", "steel", "threaten", "tumble", "upset", "aside", "awkward", "bee", "blank", "board", "button", "card", "carefully", "complain", "crap", "deeply", "discover", "drag", "dread", "effort", "entire", "fairy", "giant", "gotten", "greet", "illusion", "jeans", "leap", "liquid", "march", "mend", "nervous", "nine", "replace", "rope", "spine", "stole", "terror", "accident", "apple", "balance", "boom", "childhood", "collect", "demand", "depression", "eventually", "faint", "glare", "goal", "group", "honey", "kitchen", "laid", "limb", "machine", "mere", "mold", "murder", "nerve", "painful", "poetry", "prince", "rabbit", "shelter", "shore", "shower", "soothe", "stair", "steady", "sunlight", "tangle", "tease", "treasure", "uncle", "begun", "bliss", "canvas", "cheer", "claw", "clutch", "commit", "crimson", "crystal", "delight", "doll", "existence", "express", "fog", "football", "gay", "goose", "guard", "hatred", "illuminate", "mass", "math", "mourn", "rich", "rough", "skip", "stir", "student", "style", "support", "thorn", "tough", "yard", "yearn", "yesterday", "advice", "appreciate", "autumn", "bank", "beam", "bowl", "capture", "carve", "collapse", "confusion", "creation", "dove", "feather", "girlfriend", "glory", "government", "harsh", "hop", "inner", "loser", "moonlight", "neighbor", "neither", "peach", "pig", "praise", "screw", "shield", "shimmer", "sneak", "stab", "subject", "throughout", "thrown", "tower", "twirl", "wow", "army", "arrive", "bathroom", "bump", "cease", "cookie", "couch", "courage", "dim", "guilt", "howl", "hum", "husband", "insult", "led", "lunch", "mock", "mostly", "natural", "nearly", "needle", "nerd", "peaceful", "perfection", "pile", "price", "remove", "roam", "sanctuary", "serious", "shiny", "shook", "sob", "stolen", "tap", "vain", "void", "warrior", "wrinkle", "affection", "apologize", "blossom", "bounce", "bridge", "cheap", "crumble", "decision", "descend", "desperately", "dig", "dot", "flip", "frighten", "heartbeat", "huge", "lazy", "lick", "odd", "opinion", "process", "puzzle", "quietly", "retreat", "score", "sentence", "separate", "situation", "skill", "soak", "square", "stray", "taint", "task", "tide", "underneath", "veil", "whistle", "anywhere", "bedroom", "bid", "bloody", "burden", "careful", "compare", "concern", "curtain", "decay", "defeat", "describe", "double", "dreamer", "driver", "dwell", "evening", "flare", "flicker", "grandma", "guitar", "harm", "horrible", "hungry", "indeed", "lace", "melody", "monkey", "nation", "object", "obviously", "rainbow", "salt", "scratch", "shown", "shy", "stage", "stun", "third", "tickle", "useless", "weakness", "worship", "worthless", "afternoon", "beard", "boyfriend", "bubble", "busy", "certain", "chin", "concrete", "desk", "diamond", "doom", "drawn", "due", "felicity", "freeze", "frost", "garden", "glide", "harmony", "hopefully", "hunt", "jealous", "lightning", "mama", "mercy", "peel", "physical", "position", "pulse", "punch", "quit", "rant", "respond", "salty", "sane", "satisfy", "savior", "sheep", "slept", "social", "sport", "tuck", "utter", "valley", "wolf", "aim", "alas", "alter", "arrow", "awaken", "beaten", "belief", "brand", "ceiling", "cheese", "clue", "confidence", "connection", "daily", "disguise", "eager", "erase", "essence", "everytime", "expression", "fan", "flag", "flirt", "foul", "fur", "giggle", "glorious", "ignorance", "law", "lifeless", "measure", "mighty", "muse", "north", "opposite", "paradise", "patience", "patient", "pencil", "petal", "plate", "ponder", "possibly", "practice", "slice", "spell", "stock", "strife", "strip", "suffocate", "suit", "tender", "tool", "trade", "velvet", "verse", "waist", "witch", "aunt", "bench", "bold", "cap", "certainly", "click", "companion", "creator", "dart", "delicate", "determine", "dish", "dragon", "drama", "drum", "dude", "everybody", "feast", "forehead", "former", "fright", "fully", "gas", "hook", "hurl", "invite", "juice", "manage", "moral", "possess", "raw", "rebel", "royal", "scale", "scary", "several", "slight", "stubborn", "swell", "talent", "tea", "terrible", "thread", "torment", "trickle", "usually", "vast", "violence", "weave", "acid", "agony", "ashamed", "awe", "belly", "blend", "blush", "character", "cheat", "common", "company", "coward", "creak", "danger", "deadly", "defense", "define", "depend", "desperate", "destination", "dew", "duck", "dusty", "embarrass", "engine", "example", "explore", "foe", "freely", "frustrate", "generation", "glove", "guilty", "health", "hurry", "idiot", "impossible", "inhale", "jaw", "kingdom", "mention", "mist", "moan", "mumble", "mutter", "observe", "ode", "pathetic", "pattern", "pie", "prefer", "puff", "rape", "rare", "revenge", "rude", "scrape", "spiral", "squeeze", "strain", "sunset", "suspend", "sympathy", "thigh", "throne", "total", "unseen", "weapon", "weary"];

        var crypto = window.crypto || window.msCrypto;

        if (crypto) {
            bits = 128;

            var random = new Uint32Array(bits / 32);

            crypto.getRandomValues(random);

            var i = 0,
                l = random.length,
                n = wordCount,
                words = [],
                x, w1, w2, w3;

            for (; i < l; i++) {
                x = random[i];
                w1 = x % n;
                w2 = (((x / n) >> 0) + w1) % n;
                w3 = (((((x / n) >> 0) / n) >> 0) + w2) % n;

                words.push(allWords[w1]);
                words.push(allWords[w2]);
                words.push(allWords[w3]);
            }

            passPhrase = words.join(" ");

            crypto.getRandomValues(random);
        }

        return passPhrase;
    }
};
