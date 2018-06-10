package global.store;

import global.WagerrRate;

/**
 * Created by furszy on 3/3/18.
 */

public interface RateDbDao<T> extends AbstractDbDao<T>{

    WagerrRate getRate(String coin);


    void insertOrUpdateIfExist(WagerrRate wagerrRate);

}
