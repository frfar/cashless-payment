/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('transactions', {
    id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      autoIncrement: true,
      primaryKey: true
    },
    card_id: {
      type: DataTypes.STRING(45),
      allowNull: false,
      references: {
        model: 'cards',
        key: 'unique_id'
      }
    },
    vending_machine_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      references: {
        model: 'vending_machines',
        key: 'id'
      }
    },
    amount: {
      type: DataTypes.DECIMAL,
      allowNull: false
    },
    type: {
      type: DataTypes.INTEGER(4),
      allowNull: false,
      references: {
        model: 'transaction_types',
        key: 'id'
      }
    }
  }, {
    tableName: 'transactions'
  });
};
