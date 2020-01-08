package webservice

import CasinoLib.exceptions.UserNotFoundException
import CasinoLib.exceptions.WrongApikeyProvidedException
import CasinoLib.helpers.Exceptions
import CasinoLib.model.Amount
import CasinoLib.model.Message
import CasinoLib.services.Auth
import CasinoLib.services.CasinoLibrary
import CasinoLib.services.Logger
import helpers.AmountProcess
import helpers.Database
import helpers.RequestProcess
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.*
import java.util.concurrent.Callable


@SpringBootApplication
@RestController
open class WebServiceApplication {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Database()
            CasinoLibrary.init("src/main/resources/casinolib.properties")
            SpringApplication.run(WebServiceApplication::class.java)
        }
    }

    @Async
    @GetMapping("/accnt")
    open fun getUserBalance(@RequestHeader(name = "apikey", required = true) apikey: String,
                            @RequestParam(name = "target", required = false) login: String?): Callable<ResponseEntity<*>> {
        try {
            Logger.log(service = "Account", message = "Getting privilege level for user with apikey $apikey")
            val hasTarget: Boolean by lazy { RequestProcess.isParameterProvided(login) }
            val privilege = Auth.getUserPrivilege(apikey).level
            when (privilege) {
                1 -> {
                    Logger.log(service = "Account", message = "Banned user with apikey $apikey tried to get balance")
                    return Callable { ResponseEntity(Message("You are banned from this resource"), HttpStatus.FORBIDDEN) }
                }
                7 -> {
                    val amount = AmountProcess.getBalanceByApikey(apikey)
                    return Callable { ResponseEntity(amount, HttpStatus.OK) }
                }
                15 -> {
                    val amount: Amount = if (hasTarget)
                        AmountProcess.getBalanceByLogin(login!!)
                    else
                        AmountProcess.getBalanceByApikey(apikey)
                    if (amount.login == "N/A") return Callable { ResponseEntity(Message("Wrong Api Key provided"), HttpStatus.NOT_FOUND) }
                    return Callable { ResponseEntity(amount, HttpStatus.OK) }
                }
                else -> return Callable { ResponseEntity(Message("Not implemented for level $privilege privilege"), HttpStatus.UNPROCESSABLE_ENTITY) }
            }
        } catch (exception: Exception) {
            when (exception) {
                is WrongApikeyProvidedException -> return Callable { ResponseEntity(Message("User with apikey not found"), HttpStatus.NOT_FOUND) }
                is UserNotFoundException -> return Callable { ResponseEntity(Message("User not found"), HttpStatus.NOT_FOUND) }
            }
            Exceptions.handle(exception, "Account")
        }
        return Callable { ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR) }
    }

    @Async
    @PutMapping("/accnt")
    open fun changeUserBalance(@RequestHeader(name = "apikey", required = true) apikey: String,
                          @RequestHeader(name = "Content-Type", required = true) contentType: String,
                          @RequestBody requestBody: String): ResponseEntity<Any> {
        try {
            if (!RequestProcess.validateContentType(contentType)) return ResponseEntity(Message("Wrong Content-Type header"), HttpStatus.BAD_REQUEST)
            val privilege = Auth.getUserPrivilege(apikey).level
            when (privilege) {
                15 -> {
                    val amount = RequestProcess.bodyToAmount(requestBody)
                    if (!AmountProcess.changeZeroCheck(amount))
                        return ResponseEntity(Message("Can't change balance for user ${amount.login}"), HttpStatus.UNPROCESSABLE_ENTITY)
                    val newAmount = AmountProcess.changeBalance(amount)
                    if (newAmount.login == "N/A") return ResponseEntity(Message("User with login ${amount.login} not found"), HttpStatus.NOT_FOUND)
                    return ResponseEntity(newAmount, HttpStatus.OK)
                }
                else -> return ResponseEntity(Message("You are not allowed to change balance"), HttpStatus.FORBIDDEN)
            }
        } catch (exception: Exception) {
            when (exception) {
                is WrongApikeyProvidedException -> return ResponseEntity(Message("User with apikey not found"), HttpStatus.NOT_FOUND)
                is UserNotFoundException -> return ResponseEntity(Message("User not found"), HttpStatus.NOT_FOUND)
            }
            Exceptions.handle(exception, "Account")
            return ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Async
    @PostMapping("/accnt")
    open fun setUserBalance(@RequestHeader(name = "apikey", required = true) apikey: String,
                       @RequestHeader(name = "Content-Type", required = true) contentType: String,
                       @RequestBody requestBody: String): ResponseEntity<Any> {
        try {
            if (!RequestProcess.validateContentType(contentType)) return ResponseEntity(Message("Wrong Content-Type header"), HttpStatus.BAD_REQUEST)
            val privilege = Auth.getUserPrivilege(apikey).level
            when (privilege) {
                15 -> {
                    val amount = RequestProcess.bodyToAmount(requestBody)
                    if (!AmountProcess.zeroCheck(amount))
                        return ResponseEntity(Message("Can't change balance for user ${amount.login}"), HttpStatus.UNPROCESSABLE_ENTITY)
                    val newAmount = AmountProcess.setBalance(amount)
                    if (newAmount.login == "N/A") return ResponseEntity(Message("User with login ${amount.login} not found"), HttpStatus.NOT_FOUND)
                    return ResponseEntity(newAmount, HttpStatus.OK)
                }
                else -> return ResponseEntity(Message("You are not allowed to change balance"), HttpStatus.FORBIDDEN)
            }
        } catch (exception: Exception) {
            when (exception) {
                is WrongApikeyProvidedException -> return ResponseEntity(Message("User with apikey not found"), HttpStatus.NOT_FOUND)
                is UserNotFoundException -> return ResponseEntity(Message("User not found"), HttpStatus.NOT_FOUND)
            }
            Exceptions.handle(exception, "Account")
            return ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}