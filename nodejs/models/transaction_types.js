/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('transaction_types', {
    id: {
      type: DataTypes.INTEGER(4),
      allowNull: false,
      autoIncrement: true,
      primaryKey: true
    },
    type: {
      type: DataTypes.STRING(45),
      allowNull: false
    }
  }, {
    tableName: 'transaction_types'
  });
};
