package com.wagerr.wallet.module.bet

import com.wagerr.wallet.WagerrApplication
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import wagerr.bet.BetAction
import wagerr.bet.toBetActions

class BetActionFetcher {
    companion object Factory {
        //get all the bet events with newest odds, filter out the duplicate old one
        fun getBetActionsByEventId(eventId: String): Observable<List<BetAction>?> {
            return Observable.fromCallable {
                return@fromCallable WagerrApplication.getInstance().module.listTx().map { it.transaction }
                        .toBetActions()
                        ?.filter {
                            eventId == it.eventId }
            }.subscribeOn(Schedulers.io())
        }


    }
}