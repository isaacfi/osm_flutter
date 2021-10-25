package hamza.dali.flutter_osm_plugin

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import hamza.dali.flutter_osm_plugin.utilities.MapSnapShot
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.PluginRegistry
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.util.concurrent.atomic.AtomicInteger


class FlutterOsmPlugin :
    FlutterPlugin, ActivityAware {

    companion object {
        var mapSnapShot = MapSnapShot()


        var state = AtomicInteger(0)
        var pluginBinding: FlutterPluginBinding? = null
        var lifecycle: Lifecycle? = null
        var register: PluginRegistry.Registrar? = null
        const val VIEW_TYPE = "plugins.dali.hamza/osmview"
        const val CREATED = 1
        const val STARTED = 2
        const val RESUMED = 3
        const val PAUSED = 4
        const val STOPPED = 5
        const val DESTROYED = 6

        @JvmStatic
        fun registerWith(register: PluginRegistry.Registrar) {

            if (register.activity() == null) {
                return
            }
            this.register = register

            val flutterOsmView = FlutterOsmPlugin()
            //register.activity().application.registerActivityLifecycleCallbacks(flutterOsmView)
            register.platformViewRegistry().registerViewFactory(
                VIEW_TYPE,
                OsmFactory(
                    register.messenger(),
                    object : ProviderLifecycle {
                        override fun getLifecyle(): Lifecycle =
                            ProxyLifecycleProvider(activity = register.activity()).lifecycle
                    },
                ),
            )
        }
    }

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory(
            VIEW_TYPE,
            OsmFactory(
                binding.binaryMessenger,
                object : ProviderLifecycle {
                    override fun getLifecyle(): Lifecycle? = lifecycle

                },
            ),
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        // lifecycle?.removeObserver(this)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)


    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.e("osm", "detached activity")
        //  this.onDetachedFromActivity()
        // lifecycle?.removeObserver(this)
        // lifecycle = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.e("osm", "reAttached activity for changes")
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)

        //lifecycle?.addObserver(this)
        /*activity = binding.activity
        activity!!.application.registerActivityLifecycleCallbacks(this)

        configuration = Configuration.getInstance()
        configuration!!.load(pluginBinding!!.applicationContext,//.application,
                PreferenceManager.getDefaultSharedPreferences(pluginBinding!!.applicationContext))

        pluginBinding!!.platformViewRegistry.registerViewFactory(VIEW_TYPE,
                OsmFactory(
                        pluginBinding!!.binaryMessenger,
                        activity!!.application,
                        lifecycle,
                        activity, register))*/

    }

    override fun onDetachedFromActivity() {
        //lifecycle?.removeObserver(this)
        Configuration.getInstance().osmdroidTileCache.delete()
        lifecycle = null
        pluginBinding = null

    }

}

interface ProviderLifecycle {
    fun getLifecyle(): Lifecycle?
}

private class ProxyLifecycleProvider constructor(
    private val activity: Activity
) : Application.ActivityLifecycleCallbacks, LifecycleOwner, ProviderLifecycle {

    val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    var registrarActivityHashCode: Int = activity.hashCode()

    init {
        activity.application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.hashCode() != registrarActivityHashCode) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }


    override fun getLifecyle(): Lifecycle = lifecycle

    override fun getLifecycle(): Lifecycle = lifecycle

}