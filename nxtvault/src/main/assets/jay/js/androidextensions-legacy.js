/**
 * Created by Brandon on 6/11/2015.
 */
AndroidExtensions.getAccounts = function(){
    var accounts = localStorage['accounts'];
    if (accounts == undefined){
        return "";
    }
    else{
        return JSON.stringify(JSON.parse(accounts));
    }
};

AndroidExtensions.verifyPin = function(pin){
    var result = false;

    if (localStorage["pin"]){
        var pinData = JSON.parse(localStorage["pin"]);

        var phrase = decryptSecretPhrase(pinData.cipher, pin, pinData.checksum);

        if (phrase === "pin"){
            result = true;
        }
    }

    return result;
};