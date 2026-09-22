package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CreamCardBorder
import com.example.ui.theme.PeacockBlue
import com.example.ui.theme.TextSecondary

@Composable
fun EmptyStateView(
    message: String,
    iconSymbol: String = "🪷",
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CreamCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(iconSymbol, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionText, fontSize = 12.sp)
                }
            }
        }
    }
}
