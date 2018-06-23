package com.wagerr.wallet.module.bet

import android.util.Log
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.module.WagerrContext
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BetResultFetcher {
    companion object Factory {
        fun getBetResults(): Observable<List<BetResult>?> {
            return Observable.fromCallable {
                return@fromCallable WagerrApplication.getInstance().module.watchedSpent.toBetResults()?.sortedBy {
                    it.eventId.replace("#", "").toInt()
                }
            }.subscribeOn(Schedulers.io())
        }

        fun getBetResultByEventId(eventId: String): Observable<BetResult?> {
            return Observable.fromCallable {
                return@fromCallable WagerrApplication.getInstance().module.watchedSpent
                        .toBetResults()?.firstOrNull {
                            it.eventId == eventId
                        }
            }.subscribeOn(Schedulers.io())
        }
    }
}
