module.exports = function() {

    let Sequelize = require("sequelize");
    let config = require("./database.env.local");

    var sequelize = null;

    function initConnection() {
        if(sequelize == null || sequelize.isConnected){
            try {
                console.log("user: " + config.username + "   Pass: " + config.password);
                sequelize = new Sequelize(config.dbname, config.username, config.password, {
                    host: config.host,
                    dialect: 'mysql',
                    port: 3306,
                    define: {
                        timestamps: false
                    }
                });
            }catch(err){
              console.log("sqqqqqqqqqqqqqqqqqqqqqql!");
                throw err;
                ;
            }
        }
    }

    return {
        sequelize: function getSequalize() {
            if(sequelize == null) {
                initConnection();
            }

            return sequelize;
        }
    }
}();
