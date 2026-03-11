package com.example.annapurna.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingDialog(
    foodName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, feedback: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How was the pickup?", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Rate your experience with this donor for '$foodName'", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Star rating
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i stars",
                                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Optional feedback
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Feedback (Optional)") },
                    placeholder = { Text("Any comments for the donor?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (rating > 0) {
                        onSubmit(rating, feedback)
                    }
                },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}
