package com.wagerr.wallet.module.bet

import android.util.Log
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.BetEvent
import com.wagerr.wallet.data.bet.toBetEvents
import com.wagerr.wallet.module.WagerrContext
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BetEventFetcher {
    companion object Factory {
        //get all the bet events with newest odds, filter out the duplicate old one
        fun getCanBetBetEvents(): Observable<List<BetEvent>?> {
            return Observable.fromCallable {
                WagerrApplication.getInstance().module.watchedSpent.toBetEvents()?.filter {
                    it.timeStamp > System.currentTimeMillis() + WagerrContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME
                }?.sortedByDescending { it.timeStamp }?.distinctBy { it.eventId }?.sortedBy { it.timeStamp }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        fun getFinishedBetEvents(): Observable<List<BetEvent>?> {
            return Observable.fromCallable {
                WagerrApplication.getInstance().module.watchedSpent.toBetEvents()?.filter {
                    it.timeStamp < System.currentTimeMillis() + WagerrContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME
                }?.sortedByDescending { it.timeStamp }?.distinctBy { it.eventId }?.sortedBy { it.timeStamp }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
        //get bet event exact before the bet action with match odds
        fun getBetEventByIdAndTime(eventId: String, betTimeInMillis: Long): Observable<BetEvent?> {
            return Observable.fromCallable {
                WagerrApplication.getInstance().module.watchedSpent.filter{
                    betTimeInMillis > it.updateTime.time
                }.toBetEvents()?.filter {
                    it.eventId == eventId
                }?.sortedBy { it.timeStamp }?.last()
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
