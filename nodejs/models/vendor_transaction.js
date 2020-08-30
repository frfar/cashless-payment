/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('vendor_transactions', {
    id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      autoIncrement: true,
      primaryKey: true
    },
    card_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      references: {
        model: 'cards',
        key: 'id'
      }
    },
    vendor_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      references: {
        model: 'users',
        key: 'id'
      }
    },
    collected_amount: {
      type: DataTypes.DECIMAL,
      allowNull: false
    },
    timestamp: {
      type: DataTypes.BIGINT,
      allowNull: false
    },
  }, {
    tableName: 'vendor_transactions'
  });
};
