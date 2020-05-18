var exec = require('cordova/exec');

module.exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'MeidaSayang', 'coolMethod', [arg0]);
};

module.exports.insertUser = function (arg0, success, error) {
    exec(success, error, 'MeidaSayang', 'insertUser', [arg0]);
};

module.exports.deleteUser = function ( success, error) {
    exec(success, error, 'MeidaSayang', 'deleteUser', []);
};
module.exports.getUser = function (success, error) {
    exec(success, error, 'MeidaSayang', 'getUser', []);
};

