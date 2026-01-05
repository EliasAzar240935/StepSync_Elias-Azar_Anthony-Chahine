package com.stepsync. presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx. compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui. text.input.KeyboardType
import androidx. compose.ui.unit.dp
import com.stepsync.data.model.GoalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalDialog(
    onDismiss:  () -> Unit,
    onCreateGoal: (String, String, Int, GoalType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetSteps by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(GoalType. DAILY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Goal") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement. spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Walk 10K steps daily") }
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("Add more details...") }
                )

                // Target Steps
                OutlinedTextField(
                    value = targetSteps,
                    onValueChange = { targetSteps = it. filter { char -> char.isDigit() } },
                    label = { Text("Target Steps") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("10000") }
                )

                // Goal Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = when (selectedGoalType) {
                            GoalType.DAILY -> "Daily"
                            GoalType.WEEKLY -> "Weekly"
                            GoalType. MONTHLY -> "Monthly"
                            GoalType.CUSTOM -> "Custom"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Goal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults. TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Daily") },
                            onClick = {
                                selectedGoalType = GoalType.DAILY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Weekly") },
                            onClick = {
                                selectedGoalType = GoalType.WEEKLY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Monthly") },
                            onClick = {
                                selectedGoalType = GoalType.MONTHLY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Custom") },
                            onClick = {
                                selectedGoalType = GoalType. CUSTOM
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = targetSteps.toIntOrNull() ?: 0
                    onCreateGoal(title, description, steps, selectedGoalType)
                },
                enabled = title.isNotBlank() && targetSteps.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}