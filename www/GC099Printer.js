var exec = require('cordova/exec');

module.exports = {
    printString: function (text, resolve, reject) {
        exec(resolve, reject, "GC099Printer", "printString", [text]);
    }  
}