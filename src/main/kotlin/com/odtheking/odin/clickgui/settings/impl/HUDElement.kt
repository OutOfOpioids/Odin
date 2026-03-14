package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics

open class HudElement(
    var x: Float,
    var y: Float,
    var scale: Float,
    var enabled: Boolean = true,
    val render: GuiGraphics.(Boolean) -> Pair<Int, Int> = { _ -> 0 to 0 }
) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun draw(context: GuiGraphics, example: Boolean) {
        val xPos: Float = (x * context.guiWidth()).coerceIn(0.0f, (mc.window.width - (width * scale)))
        val yPos: Float = (y * context.guiHeight()).coerceIn(0.0f, (mc.window.height - (height * scale)))
        context.pose().pushMatrix()
        context.pose().translate(xPos, yPos)

        context.pose().scale(scale, scale)
        val (width, height) = context.render(example).let { (w, h) -> w to h }

        context.pose().popMatrix()
        if (example) context.hollowFill(xPos.toInt() - 1, yPos.toInt() - 1, (width * scale).toInt(), (height * scale).toInt(), if (isHovered()) 2 else 1, Colors.WHITE)

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x * mc.window.guiScaledWidth, y * mc.window.guiScaledHeight, width * scale, height * scale)
}
