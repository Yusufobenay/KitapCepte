package com.kitapcepte.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.component.PrimaryButton
import com.kitapcepte.core.designsystem.component.SecondaryButton
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(elevation = 10.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(KitapCepteTheme.extendedColors.surfaceWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (state.isLogin) stringResource(R.string.auth_welcome_back) else stringResource(R.string.auth_create_account),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = KitapCepteTheme.shapes.card)
                    .clip(KitapCepteTheme.shapes.card)
                    .background(KitapCepteTheme.extendedColors.surfaceWhite)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Mode Toggle (Login vs Register)
                    AuthModeSelector(
                        currentMode = state.mode,
                        onModeSelected = { onEvent(AuthUiEvent.ModeChanged(it)) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.errorMessage?.let { error ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(KitapCepteTheme.extendedColors.badgeCart.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = error.asString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KitapCepteTheme.extendedColors.badgeCart,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Register: Name Input
                    AnimatedVisibility(visible = state.isRegister) {
                        Column {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { onEvent(AuthUiEvent.NameChanged(it)) },
                                label = { Text(stringResource(R.string.auth_label_name)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                shape = KitapCepteTheme.shapes.input,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Email Input
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { onEvent(AuthUiEvent.EmailChanged(it)) },
                        label = { Text(stringResource(R.string.auth_label_email)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                        label = { Text(stringResource(R.string.auth_label_password)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (state.isRegister) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (state.isLogin) onEvent(AuthUiEvent.SubmitClicked) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    // Register: Password Confirm Input
                    AnimatedVisibility(visible = state.isRegister) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = state.passwordConfirm,
                                onValueChange = { onEvent(AuthUiEvent.PasswordConfirmChanged(it)) },
                                label = { Text(stringResource(R.string.auth_label_password_confirm)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = KitapCepteTheme.shapes.input,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { onEvent(AuthUiEvent.SubmitClicked) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Submit Button
                    if (state.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        PrimaryButton(
                            text = if (state.isLogin) stringResource(id = R.string.btn_login) else stringResource(id = R.string.btn_register),
                            onClick = { onEvent(AuthUiEvent.SubmitClicked) }
                        )
                    }

                    // Guest Login Option (displayed in Login mode)
                    if (state.isLogin) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = KitapCepteTheme.extendedColors.borderLight)
                            Text(
                                text = stringResource(R.string.auth_divider_or),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = KitapCepteTheme.extendedColors.borderLight)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SecondaryButton(
                            text = stringResource(id = R.string.btn_guest_continue),
                            onClick = { onEvent(AuthUiEvent.GuestLoginClicked) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Switch Mode Button
            TextButton(
                onClick = {
                    onEvent(AuthUiEvent.ModeChanged(if (state.isLogin) AuthMode.REGISTER else AuthMode.LOGIN))
                }
            ) {
                Text(
                    text = if (state.isLogin) stringResource(R.string.auth_switch_to_register) else stringResource(R.string.auth_switch_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AuthModeSelector(
    currentMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KitapCepteTheme.shapes.buttonPill)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AuthModeTab(
            text = stringResource(R.string.auth_tab_login),
            isSelected = currentMode == AuthMode.LOGIN,
            onClick = { onModeSelected(AuthMode.LOGIN) },
            modifier = Modifier.weight(1f)
        )
        AuthModeTab(
            text = stringResource(R.string.auth_tab_register),
            isSelected = currentMode == AuthMode.REGISTER,
            onClick = { onModeSelected(AuthMode.REGISTER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AuthModeTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabText"
    )

    Box(
        modifier = modifier
            .clip(KitapCepteTheme.shapes.buttonPill)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    KitapCepteTheme {
        AuthScreen(
            state = AuthUiState(mode = AuthMode.LOGIN),
            onEvent = {}
        )
    }
}
