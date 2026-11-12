package hu.mostoha.mobile.kmp.huki

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import hu.mostoha.mobile.kmp.huki.util.toIncomingLink

/**
 * Receives links and files from other apps and forwards them to [MainActivity] in HuKi's own task.
 * Opened directly, `singleTask` alone may still start a second HuKi inside the caller's task.
 */
class IncomingLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.toIncomingLink() != null) {
            val forwarded = Intent(intent)
                .setClass(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or (intent.flags and URI_GRANT_FLAGS))
            startActivity(forwarded)
        }
        finish()
    }

    private companion object {
        /**
         * Only the access the caller granted can be passed on, adding more throws a SecurityException.
         */
        const val URI_GRANT_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    }
}
