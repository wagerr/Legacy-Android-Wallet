package com.wagerr.wallet.module.bet

import android.util.Log
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.module.WagerrContext
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.wagerrj.core.Transaction

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