package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.ModuleManager.hudSettingsCache
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlin.math.sign
import com.odtheking.odin.utils.ui.mouseX as odinMouseX
import com.odtheking.odin.utils.ui.mouseY as odinMouseY

object HudManager : Screen(Component.literal("HUD Manager")) {

    private var dragging: HudElement? = null

    private var deltaX = 0f
    private var deltaY = 0f

    override fun init() {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled) {
                hud.value.x = hud.value.x.coerceIn(0.0f, (mc.window.width - (hud.value.width * hud.value.scale)) / mc.window.guiScaledWidth)
                hud.value.y = hud.value.y.coerceIn(0.0f, (mc.window.height - (hud.value.height * hud.value.scale)) / mc.window.guiScaledHeight)
            }
        }
        super.init()
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)

        dragging?.let {
            it.x = ((odinMouseX + deltaX) / mc.window.guiScaledWidth).coerceIn(0f, (mc.window.width - (it.width * it.scale)) / mc.window.guiScaledWidth)
            it.y = ((odinMouseY + deltaY) / mc.window.guiScaledHeight).coerceIn(0f, (mc.window.height - (it.height * it.scale)) / mc.window.guiScaledHeight)
        }

        context.pose().pushMatrix()
        val sf = mc.window.guiScale
        context.pose().scale(1f / sf, 1f / sf)

        for (hud in hudSettingsCache) {
            if (hud.isEnabled) hud.value.draw(context, true)
        }

        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hoveredHud ->
            val xPos: Float = hoveredHud.value.x * context.guiWidth()
            val yPos: Float = hoveredHud.value.y * context.guiHeight()
            context.pose().pushMatrix()
            context.pose().translate(
                (xPos + hoveredHud.value.width * hoveredHud.value.scale + 10f),
                yPos,
            )
            context.pose().scale(2f, 2f)
            context.drawString(mc.font, hoveredHud.name, 0, 0, Colors.WHITE.rgba)
            context.drawWordWrap(mc.font, Component.literal(hoveredHud.description), 0, 10, 150, Colors.WHITE.rgba)
            context.pose().popMatrix()
        }

        context.pose().popMatrix()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val actualAmount = verticalAmount.sign.toFloat() * 0.2f
        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            hovered.value.scale = (hovered.value.scale + actualAmount).coerceIn(1f, 10f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            val xPos: Float = hovered.value.x * mc.window.guiScaledWidth
            val yPos: Float = hovered.value.y * mc.window.guiScaledHeight
            dragging = hovered.value

            deltaX = (xPos - odinMouseX)
            deltaY = (yPos - odinMouseY)
            return true
        }

        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        dragging = null
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            when (keyEvent.key) {
                GLFW.GLFW_KEY_EQUAL -> hovered.value.scale = (hovered.value.scale + 0.1f).coerceIn(1f, 10f)
                GLFW.GLFW_KEY_MINUS -> hovered.value.scale = (hovered.value.scale - 0.1f).coerceIn(1f, 10f)
                GLFW.GLFW_KEY_RIGHT -> hovered.value.x += 0.005f
                GLFW.GLFW_KEY_LEFT -> hovered.value.x -= 0.005f
                GLFW.GLFW_KEY_UP -> hovered.value.y -= 0.01f
                GLFW.GLFW_KEY_DOWN -> hovered.value.y += 0.01f
            }
        }

        return super.keyPressed(keyEvent)
    }

    override fun onClose() {
        ModuleManager.saveConfigurations()
        super.onClose()
    }

    fun resetHUDS() {
        hudSettingsCache.forEach {
            it.value.x = 0.005f
            it.value.y = 0.01f
            it.value.scale = 2f
        }
    }

    override fun isPauseScreen(): Boolean = false
}
