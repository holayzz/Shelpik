package com.holayzz.github.shelpik

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class SnowfallView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val snowflakes = mutableListOf<Snowflake>()
    private val paint = Paint().apply {
        color = Color.WHITE
        alpha = 100 // Полупрозрачные снежинки
        isAntiAlias = true
    }

    private val random = Random(System.currentTimeMillis())

    init {
        // Создаем начальные снежинки
        for (i in 0 until 50) {
            snowflakes.add(createSnowflake())
        }
    }

    private fun createSnowflake(): Snowflake {
        return Snowflake(
            x = random.nextFloat() * width,
            y = random.nextFloat() * height,
            size = random.nextFloat() * 3 + 1f,
            speed = random.nextFloat() * 3 + 1f,
            sway = random.nextFloat() * 2 - 1f
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        snowflakes.clear()
        for (i in 0 until 50) {
            snowflakes.add(createSnowflake())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем снежинки
        for (snowflake in snowflakes) {
            paint.alpha = (snowflake.size * 40).toInt()
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.size, paint)
        }

        // Обновляем позиции
        updateSnowflakes()

        // Перерисовываем
        invalidate()
    }

    private fun updateSnowflakes() {
        for (snowflake in snowflakes) {
            // Движение вниз
            snowflake.y += snowflake.speed

            // Легкое колебание из стороны в сторону
            snowflake.x += snowflake.sway * 0.5f

            // Если снежинка упала за экран, создаем новую сверху
            if (snowflake.y > height) {
                snowflake.y = 0f
                snowflake.x = random.nextFloat() * width
            }

            // Если снежинка ушла за боковые границы, возвращаем ее
            if (snowflake.x < 0) snowflake.x = width.toFloat()
            if (snowflake.x > width) snowflake.x = 0f
        }
    }

    private data class Snowflake(
        var x: Float,
        var y: Float,
        val size: Float,
        val speed: Float,
        val sway: Float
    )
}