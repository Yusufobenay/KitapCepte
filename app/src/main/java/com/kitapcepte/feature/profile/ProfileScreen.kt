package com.kitapcepte.feature.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.component.AppTopBar
import com.kitapcepte.core.designsystem.component.PrimaryButton
import com.kitapcepte.core.designsystem.component.SecondaryButton
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.SavedCard
import com.kitapcepte.domain.model.User

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top Bar
            AppTopBar(
                title = stringResource(id = R.string.profile_title)
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Header
                ProfileHeader(
                    initials = state.userInitials,
                    name = state.user?.name ?: if (state.isGuest) stringResource(R.string.profile_guest_title) else "",
                    email = state.user?.email.orEmpty(),
                    isGuest = state.isGuest
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isGuest) {
                    // Guest Mode Info & Login Prompt
                    GuestModeCard(
                        onLoginClick = { onEvent(ProfileUiEvent.LoginClicked) }
                    )
                    GuestBenefitsCard() //
                } else {
                    ReadingGoalCard()
                    // 1. User Info Card
                    UserInfoCard(
                        user = state.user,
                        isEditing = state.isEditingName,
                        nameInput = state.nameInput,
                        nameError = state.nameError?.asString(),
                        isLoading = state.isActionLoading,
                        onToggleEdit = { onEvent(ProfileUiEvent.ToggleEditName(it)) },
                        onNameChange = { onEvent(ProfileUiEvent.NameInputChanged(it)) },
                        onSaveName = { onEvent(ProfileUiEvent.SaveNameClicked) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Security & Password Update Card
                    SecurityCard(
                        isExpanded = state.isChangingPassword,
                        currentPassword = state.currentPasswordInput,
                        newPassword = state.newPasswordInput,
                        confirmPassword = state.confirmPasswordInput,
                        currentPasswordVisible = state.currentPasswordVisible,
                        newPasswordVisible = state.newPasswordVisible,
                        confirmPasswordVisible = state.confirmPasswordVisible,
                        passwordError = state.passwordError?.asString(),
                        isLoading = state.isActionLoading,
                        onToggleExpand = { onEvent(ProfileUiEvent.ToggleChangePassword(it)) },
                        onCurrentPasswordChange = { onEvent(ProfileUiEvent.CurrentPasswordChanged(it)) },
                        onNewPasswordChange = { onEvent(ProfileUiEvent.NewPasswordChanged(it)) },
                        onConfirmPasswordChange = { onEvent(ProfileUiEvent.ConfirmPasswordChanged(it)) },
                        onToggleCurrentVisibility = { onEvent(ProfileUiEvent.ToggleCurrentPasswordVisibility) },
                        onToggleNewVisibility = { onEvent(ProfileUiEvent.ToggleNewPasswordVisibility) },
                        onToggleConfirmVisibility = { onEvent(ProfileUiEvent.ToggleConfirmPasswordVisibility) },
                        onSubmitPassword = { onEvent(ProfileUiEvent.SubmitChangePasswordClicked) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Saved Cards Card
                    SavedCardsCard(
                        cards = state.savedCards,
                        isAddingCard = state.isAddingCard,
                        cardHolder = state.cardHolderInput,
                        cardNumber = state.cardNumberInput,
                        formattedCardNumber = state.formattedCardNumberInput,
                        cardExpiry = state.cardExpiryInput,
                        formattedExpiry = state.formattedExpiryInput,
                        cardBrandPreview = state.cardBrandPreview,
                        cardError = state.cardError?.asString(),
                        isLoading = state.isActionLoading,
                        onToggleAddCard = { onEvent(ProfileUiEvent.ToggleAddCard(it)) },
                        onHolderChange = { onEvent(ProfileUiEvent.CardHolderChanged(it)) },
                        onNumberChange = { onEvent(ProfileUiEvent.CardNumberChanged(it)) },
                        onExpiryChange = { onEvent(ProfileUiEvent.CardExpiryChanged(it)) },
                        onSubmitCard = { onEvent(ProfileUiEvent.SubmitAddCardClicked) },
                        onDeleteCard = { onEvent(ProfileUiEvent.DeleteCardClicked(it)) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Account Actions Card (Logout & Switch to Guest)
                    AccountActionsCard(
                        onSwitchToGuest = { onEvent(ProfileUiEvent.SwitchToGuestClicked) },
                        onLogoutClick = { onEvent(ProfileUiEvent.ShowLogoutDialog(true)) }
                    )
                }

                // Bottom bar spacer to prevent content overlap
                Spacer(modifier = Modifier.height(104.dp))
            }
        }

        // Logout Confirmation Dialog
        if (state.showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(ProfileUiEvent.ShowLogoutDialog(false)) },
                shape = KitapCepteTheme.shapes.card,
                containerColor = KitapCepteTheme.extendedColors.surfaceWhite,
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_logout_confirm_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.profile_logout_confirm_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(ProfileUiEvent.LogoutClicked) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.profile_logout),
                            color = KitapCepteTheme.extendedColors.badgeCart,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onEvent(ProfileUiEvent.ShowLogoutDialog(false)) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.btn_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Profile Header
// ---------------------------------------------------------------------------
@Composable
private fun ProfileHeader(
    initials: String,
    name: String,
    email: String,
    isGuest: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(KitapCepteTheme.extendedColors.surfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            if (isGuest) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (name.isNotBlank()) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (email.isNotBlank()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Role Badge
        Box(
            modifier = Modifier
                .clip(KitapCepteTheme.shapes.pill)
                .background(
                    if (isGuest) KitapCepteTheme.extendedColors.badgeTopItem.copy(alpha = 0.9f)
                    else KitapCepteTheme.extendedColors.surfaceWhite.copy(alpha = 0.25f)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGuest) stringResource(R.string.profile_role_guest) else stringResource(R.string.profile_role_member),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGuest) KitapCepteTheme.extendedColors.onBadgeTopItem else Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 1. User Info Card
// ---------------------------------------------------------------------------
@Composable
private fun UserInfoCard(
    user: User?,
    isEditing: Boolean,
    nameInput: String,
    nameError: String?,
    isLoading: Boolean,
    onToggleEdit: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Title + Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.profile_section_user_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isEditing) {
                    TextButton(onClick = { onToggleEdit(true) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.profile_edit_name),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isEditing) {
                // Name display row
                ProfileDetailRow(
                    label = stringResource(id = R.string.profile_label_name),
                    value = user?.name.orEmpty()
                )

                HorizontalDivider(
                    color = KitapCepteTheme.extendedColors.borderLight.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Email display row
                ProfileDetailRow(
                    label = stringResource(id = R.string.profile_label_email),
                    value = user?.email.orEmpty()
                )
            } else {
                // Editing Form
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.profile_label_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = KitapCepteTheme.extendedColors.badgeCart) } },
                    shape = KitapCepteTheme.shapes.input,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(
                        text = stringResource(id = R.string.profile_cancel),
                        onClick = { onToggleEdit(false) },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = stringResource(id = R.string.profile_save),
                        onClick = onSaveName,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Security & Password Card
// ---------------------------------------------------------------------------
@Composable
private fun SecurityCard(
    isExpanded: Boolean,
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    currentPasswordVisible: Boolean,
    newPasswordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    passwordError: String?,
    isLoading: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleCurrentVisibility: () -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    onSubmitPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.profile_section_security),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = { onToggleExpand(!isExpanded) }) {
                    Text(
                        text = if (isExpanded) stringResource(R.string.profile_cancel) else stringResource(R.string.profile_change_password),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    if (passwordError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(KitapCepteTheme.extendedColors.badgeCart.copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = passwordError,
                                style = MaterialTheme.typography.bodySmall,
                                color = KitapCepteTheme.extendedColors.badgeCart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Current Password
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = onCurrentPasswordChange,
                        label = { Text(stringResource(R.string.profile_current_password)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleCurrentVisibility) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text(stringResource(R.string.profile_new_password)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleNewVisibility) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirm New Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text(stringResource(R.string.profile_confirm_password)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleConfirmVisibility) {
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PrimaryButton(
                        text = stringResource(id = R.string.profile_update_password_btn),
                        onClick = onSubmitPassword,
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Saved Cards Card
// ---------------------------------------------------------------------------
@Composable
private fun SavedCardsCard(
    cards: List<SavedCard>,
    isAddingCard: Boolean,
    cardHolder: String,
    cardNumber: String,
    formattedCardNumber: String,
    cardExpiry: String,
    formattedExpiry: String,
    cardBrandPreview: String,
    cardError: String?,
    isLoading: Boolean,
    onToggleAddCard: (Boolean) -> Unit,
    onHolderChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onSubmitCard: () -> Unit,
    onDeleteCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.profile_section_saved_cards),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = { onToggleAddCard(!isAddingCard) }) {
                    Text(
                        text = if (isAddingCard) stringResource(R.string.profile_cancel) else stringResource(R.string.profile_add_card),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Card Form
            AnimatedVisibility(
                visible = isAddingCard,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (cardError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(KitapCepteTheme.extendedColors.badgeCart.copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = cardError,
                                style = MaterialTheme.typography.bodySmall,
                                color = KitapCepteTheme.extendedColors.badgeCart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Card Holder Name
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = onHolderChange,
                        label = { Text(stringResource(R.string.profile_card_holder_label)) },
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card Number
                    OutlinedTextField(
                        value = formattedCardNumber,
                        onValueChange = onNumberChange,
                        label = { Text(stringResource(R.string.profile_card_number_label)) },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(KitapCepteTheme.shapes.tag)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cardBrandPreview,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expiry
                    OutlinedTextField(
                        value = formattedExpiry,
                        onValueChange = onExpiryChange,
                        label = { Text(stringResource(R.string.profile_card_expiry_label)) },
                        shape = KitapCepteTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PrimaryButton(
                        text = stringResource(id = R.string.profile_save_card_btn),
                        onClick = onSubmitCard,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Cards List
            if (cards.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.profile_no_cards),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cards.forEach { card ->
                        SavedCardItem(
                            card = card,
                            onDelete = { onDeleteCard(card.id) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Single Saved Card View
// ---------------------------------------------------------------------------
@Composable
private fun SavedCardItem(
    card: SavedCard,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(KitapCepteTheme.shapes.tag)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.cardBrand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = card.maskedNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = card.cardHolder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SKT: ${card.formattedExpiry}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.profile_delete_card_desc),
                    tint = KitapCepteTheme.extendedColors.badgeCart,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Account Actions Card
// ---------------------------------------------------------------------------
@Composable
private fun AccountActionsCard(
    onSwitchToGuest: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_section_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Switch to guest button
            SecondaryButton(
                text = stringResource(id = R.string.profile_switch_to_guest),
                onClick = onSwitchToGuest
            )

            // Logout button
            PrimaryButton(
                text = stringResource(id = R.string.profile_logout),
                onClick = onLogoutClick,
                leadingIcon = Icons.AutoMirrored.Outlined.Logout
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Guest Mode View
// ---------------------------------------------------------------------------
@Composable
private fun GuestModeCard(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.profile_guest_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.profile_guest_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = stringResource(id = R.string.btn_login_or_register),
                onClick = onLoginClick
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helper: Detail Row
// ---------------------------------------------------------------------------
@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    KitapCepteTheme {
        ProfileScreen(
            state = ProfileUiState(
                user = User(id = 1, email = "serdar@kitapcepte.com", name = "Serdar Akçay"),
                savedCards = listOf(
                    SavedCard(
                        id = 1,
                        userId = 1,
                        cardHolder = "SERDAR AKÇAY",
                        maskedNumber = "•••• •••• •••• 4242",
                        cardBrand = "VISA",
                        expiryMonth = "12",
                        expiryYear = "28"
                    )
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenGuestPreview() {
    KitapCepteTheme {
        ProfileScreen(
            state = ProfileUiState(
                isGuest = true
            ),
            onEvent = {}
        )
    }
}

@Composable
fun ReadingGoalCard(
    currentBooks: Int = 18,
    targetBooks: Int = 25,
    modifier: Modifier = Modifier
) {
    val progress = (currentBooks.toFloat() / targetBooks.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 2026 Okuma Hedefi",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "%$percentage",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Text(
                text = "$targetBooks kitaptan $currentBooks tanesi tamamlandı",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun GuestBenefitsCard(
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✨ KitapCepte Ayrıcalıkları",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            BenefitItem(emoji = "🔖", title = "Kişisel Kitaplığım", desc = "Okuduğun ve favorilediğin kitapları listele")
            BenefitItem(emoji = "🎯", title = "Yıllık Okuma Hedefi", desc = "Hedefler koy ve ilerlemeni anlık takip et")
            BenefitItem(emoji = "🔔", title = "İndirim Fırsatları", desc = "Takip ettiğin kitaplar indirime girdiğinde yakala")
        }
    }
}

@Composable
private fun BenefitItem(emoji: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(text = emoji, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Column {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                text = desc,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}