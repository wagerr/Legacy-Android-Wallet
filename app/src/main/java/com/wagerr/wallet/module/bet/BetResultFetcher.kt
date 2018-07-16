package com.wagerr.wallet.module.bet

import com.wagerr.wallet.WagerrApplication
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import wagerr.bet.BetResult
import wagerr.bet.toBetResults

class BetResultFetcher {
    companion object Factory {
        fun getBetResults(): Observable<List<BetResult>?> {
            return Observable.fromCallable {
                return@fromCallable WagerrApplication.getInstance().module.watchedSpent.toBetResults()?.sortedBy {
                    it.transaction.updateTime
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
