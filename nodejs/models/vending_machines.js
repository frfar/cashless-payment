/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('vending_machines', {
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
    unique_id: {
      type: DataTypes.STRING(45),
      allowNull: false
    }
  }, {
    tableName: 'vending_machines'
  });
};
