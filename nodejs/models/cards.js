/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('cards', {
    id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      autoIncrement: true,
      primaryKey: true
    },
    unique_id: {
      type: DataTypes.STRING(45),
      allowNull: false,
      unique: true
    },
    amount: {
      type: DataTypes.DECIMAL,
      allowNull: false
    },
    user_id: {
      type: DataTypes.INTEGER(20),
      allowNull: false,
      references: {
        model: 'users',
        key: 'id'
      }
    }
  }, {
    tableName: 'cards'
  });
};
