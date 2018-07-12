package com.wagerr.wallet.module.wallet;

import com.wagerr.wallet.WagerrApplication;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import global.BackupHelper;
import global.WagerrCoreContext;
import global.utils.Iso8601Format;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by furszy on 6/29/17.
 */

public class WalletBackupHelper implements BackupHelper{

    public File determineBackupFile(String extraData) {
        WagerrCoreContext.Files.EXTERNAL_WALLET_BACKUP_DIR.mkdirs();
        checkState(WagerrCoreContext.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory(), "%s is not a directory", WagerrCoreContext.Files.EXTERNAL_WALLET_BACKUP_DIR);

        final DateFormat dateFormat = Iso8601Format.newDateFormat();
        dateFormat.setTimeZone(TimeZone.getDefault());

        String appName = WagerrApplication.getInstance().getVersionName();

        for (int i = 0; true; i++) {
            final StringBuilder filename = new StringBuilder(WagerrCoreContext.Files.getExternalWalletBackupFileName(appName));
            filename.append('-');
            filename.append(dateFormat.format(new Date()));
            if (extraData!=null){
                filename.append("-"+extraData);
            }
            if (i > 0)
                filename.append(" (").append(i).append(')');

            final File file = new File(WagerrCoreContext.Files.EXTERNAL_WALLET_BACKUP_DIR, filename.toString());
            if (!file.exists())
                return file;
        }
    }

}
