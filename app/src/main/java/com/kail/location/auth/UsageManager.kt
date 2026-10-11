package com.kail.location.auth

import android.content.Context
import com.kail.location.utils.KailLog

object UsageManager {
    private const val TAG = "UsageManager"

    /**
     * 开机后需要等待的最短时长（毫秒）。ROOT 模式靠 ptrace 注入 system_server，
     * 而 system_server 在刚开机的几十秒内仍在大量初始化/JIT 编译，此时注入会让
     * 远程函数调用长时间不返回，触发 watchdog 把 system_server 重启（设备卡死/重启）。
     * 因此 ROOT 模式启动模拟前要求开机已超过该时长。
     */
    private const val BOOT_READY_THRESHOLD_MS = 100_000L

    fun init(context: Context) {}

    /**
     * 系统是否已就绪（仅对需要注入 system_server 的 ROOT 模式有意义）。
     * 返回值：是否就绪 + 还需等待的秒数（已就绪时为 0）。
     */
    fun systemReadiness(): Pair<Boolean, Int> {
        val uptimeMs = android.os.SystemClock.elapsedRealtime()
        if (uptimeMs >= BOOT_READY_THRESHOLD_MS) return true to 0
        val remainSec = ((BOOT_READY_THRESHOLD_MS - uptimeMs + 999) / 1000).toInt()
        return false to remainSec
    }

    /** 开机就绪门控所要求的开机时长（秒），用于提示文案。 */
    fun bootReadyThresholdSeconds(): Int = (BOOT_READY_THRESHOLD_MS / 1000).toInt()

    /**
     * 检查是否可以开始模拟（不消耗次数）。
     *
     * YG 分支保留账户与订阅界面，但模拟功能不依赖登录、订阅、网络或剩余次数。
     * 访问策略集中在这里，后续合并 main 时只需维护这个统一入口。
     */
    suspend fun canStartSimulation(context: Context?): Boolean {
        KailLog.i(context, TAG, "canStartSimulation=true: 登录与次数限制已取消")
        return true
    }

    /**
     * 记录一次模拟启动。
     *
     * YG 分支不扣减本地或服务端次数，也不发起鉴权请求，始终返回成功。
     */
    suspend fun consumeSimulation(context: Context?): Boolean {
        KailLog.i(context, TAG, "consumeSimulation=true: 不执行次数扣减")
        return true
    }
}
