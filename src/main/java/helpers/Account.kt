package helpers

import CasinoLib.model.Amount

object Account {
    fun getBalanceByLogin(login: String?): Amount {
        val query = "select balance from users where login = ?"
        val preparedStatement = Database.conn.prepareStatement(query)
        preparedStatement.setString(1, login)
        val resultSet = preparedStatement.executeQuery()
        resultSet.next()
        val balance = resultSet.getLong("balance")
        resultSet.close()
        return Amount(login = login!!, amount = balance)
    }

    fun getBalanceByApikey(apikey: String): Amount {
        val query = "select login, balance from users where apikey = ?"
        val preparedStatement = Database.conn.prepareStatement(query)
        preparedStatement.setString(1, apikey)
        val resultSet = preparedStatement.executeQuery()
        resultSet.next()
        val login = resultSet.getString("login")
        val balance = resultSet.getLong("balance")
        resultSet.close()
        return Amount(login = login, amount = balance)
    }

    fun changeBalance(amount: Amount): Amount {
        val query = "update users set balance = balance + (?) where login = ? returning balance"
        val preparedStatement = Database.conn.prepareStatement(query)
        preparedStatement.setLong(1, amount.amount)
        preparedStatement.setString(2, amount.login)
        val resultSet = preparedStatement.executeQuery()
        resultSet.next()
        val returnAmount = Amount(amount.login, resultSet.getLong("balance"))
        resultSet.close()
        return returnAmount
    }

    fun setBalance(amount: Amount): Amount {
        val query = "update users set balance = ? where login = ? returning balance"
        val preparedStatement = Database.conn.prepareStatement(query)
        preparedStatement.setLong(1, amount.amount)
        preparedStatement.setString(2, amount.login)
        val resultSet = preparedStatement.executeQuery()
        resultSet.next()
        val returnAmount = Amount(amount.login, resultSet.getLong("balance"))
        resultSet.close()
        return returnAmount
    }
}