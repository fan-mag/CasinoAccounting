package helpers

import CasinoLib.exceptions.UserNotFoundException
import CasinoLib.model.Amount
import CasinoLib.services.Logger
import org.postgresql.util.PSQLException

object AmountProcess {
    fun getBalanceByLogin(login: String): Amount {
        Logger.log(service = "Account", message = "Getting balance for user with login $login")
        return try {
            val amount = Account.getBalanceByLogin(login)
            Logger.log(service = "Account", message = "User with login $login has balance: ${amount.amount}")
            amount
        } catch (sqlException: PSQLException) {
            Logger.log(service = "Account", message = "User with login $login not found")
            throw UserNotFoundException()
        }
    }

    fun getBalanceByApikey(apikey: String): Amount {
        Logger.log(service = "Account", message = "Getting balance for user with apikey $apikey")
        return try {
            val amount = Account.getBalanceByApikey(apikey)
            Logger.log(service = "Account", message = "User with apikey $apikey has balance: ${amount.amount}")
            amount
        } catch (sqlException: PSQLException)
        {
            Logger.log(service = "Account", message = "User with apikey $apikey was not found")
            throw UserNotFoundException()
        }
    }

    fun changeZeroCheck(amount: Amount): Boolean {
        Logger.log(service = "Account", message = "Checking that changing amount for user ${amount.login} is possible")
        val currentAmount = getBalanceByLogin(amount.login)
        val result = currentAmount.amount.plus(amount.amount)
        Logger.log(service = "Account", message = "The result amount will be $result")
        return (result >= 0)
    }

    fun changeBalance(amount: Amount): Amount {
        Logger.log(service = "Account", message = "Changing balance for user ${amount.login}, delta is ${amount.amount}")
        return try {
            val newAmount = Account.changeBalance(amount)
            Logger.log(service = "Account", message = "New user ${newAmount.login} balance is ${newAmount.amount}")
            newAmount
        } catch (sqlException: PSQLException) {
            Logger.log(service = "Account", message = "User ${amount.login} not found")
            throw UserNotFoundException()
        }
    }

    fun zeroCheck(amount: Amount): Boolean {
        Logger.log(service = "Account", message = "Checking that set amount ${amount.amount} is above zero")
        return (amount.amount >= 0)
    }

    fun setBalance(amount: Amount): Amount {
        Logger.log(service = "Account", message = "Set balance for user ${amount.login} to ${amount.amount}")
        return try {
            val newAmount = Account.setBalance(amount)
            Logger.log(service = "Account", message = "New user ${newAmount.login} balance is ${newAmount.amount}")
            newAmount
        } catch (sqlException: PSQLException) {
            Logger.log(service = "Account", message = "User ${amount.login} not found")
            throw UserNotFoundException()
        }
    }
}
