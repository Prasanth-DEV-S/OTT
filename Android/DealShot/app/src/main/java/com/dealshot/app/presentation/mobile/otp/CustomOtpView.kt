package com.dealshot.app.presentation.mobile.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.dealshot.app.ui.theme.VividOrange

@Composable
fun CustomOtpView(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    BasicTextField(
        value = otpText,

        onValueChange = {
            if (
                it.length <= 6 &&
                it.all(Char::isDigit)
            ) {
                onOtpTextChange(it)
            }
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),

        modifier = modifier,

        decorationBox = {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),

                modifier = Modifier.fillMaxWidth()
            ) {

                repeat(6) { index ->

                    val char =
                        otpText
                            .getOrNull(index)
                            ?.toString()
                            ?: ""

                    val isActive =
                        index == otpText.length ||
                                (
                                        index == 5 &&
                                                otpText.length == 6
                                        )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                Color(0xFF0F0E15),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 2.dp,
                                color =
                                    if (isActive)
                                        VividOrange
                                    else
                                        Color(0xFF1E1C29),
                                shape =
                                    RoundedCornerShape(14.dp)
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = char,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}