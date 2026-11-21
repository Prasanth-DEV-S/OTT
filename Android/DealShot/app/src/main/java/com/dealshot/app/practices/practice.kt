package com.dealshot.app.practices

import android.R.attr.password
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

class practice {
}
data class LoginUi(val email: String = "", val password: String = "") {
    val emailValid get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val canSubmit get() = emailValid && password.length >= 6
}

@Composable
fun LoginScreen(ui: LoginUi, onEmail: (String) -> Unit, onPassword: (String) -> Unit, onSubmit: () -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = ui.email, onValueChange = onEmail, label = { Text("Email") },
            isError = ui.email.isNotEmpty() && !ui.emailValid, singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = ui.password, onValueChange = onPassword, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), singleLine = true
        )
        Button(onClick = onSubmit, enabled = ui.canSubmit, modifier = Modifier.fillMaxWidth()) { Text("Login") }
    }

    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = password, onValueChange = onPassword,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Default.VisibilityOff
                else Icons.Default.Visibility, contentDescription = "Toggle password")
            }
        }
    )


}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(tags: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            FilterChip(selected = tag in selected, onClick = { onToggle(tag) }, label = { Text(tag) })
        }
    }
}
@Composable
fun LoadingButton(text: String, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = !loading, modifier = modifier) {
        if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
        else Text(text)
    }
}




