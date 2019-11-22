/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('offline_transactions', {
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
    vm_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      references: {
        model: 'vending_machines',
        key: 'id'
      }
    },
    remaining_amount: {
      type: DataTypes.DECIMAL,
      allowNull: false
    },
    timestamp: {
      type: DataTypes.BIGINT,
      allowNull: false
    },
    prev_vm_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false
    },
    prev_remaining_amount: {
      type: DataTypes.DECIMAL,
      allowNull: false
    },
    prev_timestamp: {
      type: DataTypes.BIGINT,
      allowNull: false
    },
    complete: {
      type: DataTypes.INTEGER(4),
      allowNull: false
    }
  }, {
    tableName: 'offline_transactions'
  });
};
