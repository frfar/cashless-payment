/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('users', {
    id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      autoIncrement: true,
      primaryKey: true
    },
    name: {
      type: DataTypes.STRING(45),
      allowNull: false
    },
    email: {
      type: DataTypes.STRING(45),
      allowNull: false
    },
    contact: {
      type: DataTypes.STRING(15),
      allowNull: false
    }, 
    passhash: {
      type: DataTypes.STRING(255),
      allowNull: false
    },
    is_admin: {
      type: DataTypes.TINYINT(1),
      allowNull: false,
      defaultValue: 0
    }
  }, {
    tableName: 'users'
  });
};
