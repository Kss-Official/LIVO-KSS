import os
import re

screens = [
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddTaskScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddEventScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddGoalScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddHabitScreen.tsx"
]

imports = """
import { DatePickerField } from '../components/forms/DatePickerField';
import { TimePickerField } from '../components/forms/TimePickerField';
import { SelectionModal } from '../components/forms/SelectionModal';
import { PrioritySelector } from '../components/forms/PrioritySelector';
import { SubtaskManager } from '../components/forms/SubtaskManager';
import { AttachmentPicker } from '../components/forms/AttachmentPicker';
"""

def process_file(filepath):
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return
        
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add imports
    if "DatePickerField" not in content:
        content = re.sub(
            r"(import React.*?;\n)", 
            r"\1" + imports.strip() + "\n", 
            content, 
            count=1
        )

    # 2. Change selectedDate and selectedTime initial state (only if they exist)
    content = re.sub(
        r"const \[selectedDate, setSelectedDate\] = useState\([^)]*\);",
        r"const [selectedDate, setSelectedDate] = useState<Date>(new Date());",
        content
    )
    content = re.sub(
        r"const \[selectedTime, setSelectedTime\] = useState\([^)]*\);",
        r"const [selectedTime, setSelectedTime] = useState<Date>(new Date());",
        content
    )

    # 3. Add attachments state and modal states
    state_additions = """
  const [attachments, setAttachments] = useState<any[]>([]);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [showReminderModal, setShowReminderModal] = useState(false);
  const [showRepeatModal, setShowRepeatModal] = useState(false);
"""
    if "const [attachments" not in content:
        content = re.sub(
            r"(const \[(?:taskTitle|title|eventTitle|goalTitle).*?\n)",
            r"\1" + state_additions,
            content
        )

    # 5. Replace Date/Time fields
    content = re.sub(
        r"<View style=\{styles\.colHalf\}>\s*<Text style=\{styles\.fieldLabel\}>Date</Text>.*?</View>",
        r"<View style={styles.colHalf}>\n              <DatePickerField value={selectedDate} onChange={setSelectedDate} />\n            </View>",
        content,
        flags=re.DOTALL
    )
    
    content = re.sub(
        r"<View style=\{styles\.colHalf\}>\s*<Text style=\{styles\.fieldLabel\}>Time</Text>.*?</View>",
        r"<View style={styles.colHalf}>\n              <TimePickerField value={selectedTime} onChange={setSelectedTime} />\n            </View>",
        content,
        flags=re.DOTALL
    )

    # 6. Replace Priority row
    content = re.sub(
        r"<Text style=\{styles\.fieldLabel\}>Priority</Text>\s*<View style=\{styles\.priorityRow\}>.*?</View>",
        r"<Text style={styles.fieldLabel}>Priority</Text>\n          <PrioritySelector selectedPriority={selectedPriority} onSelect={setSelectedPriority} />",
        content,
        flags=re.DOTALL
    )
    
    # 7. Replace TouchableOpacity onPress for Modals
    def replacer_category(m):
        return m.group(0).replace("<TouchableOpacity style={styles.dropdownBox}>", "<TouchableOpacity style={styles.dropdownBox} onPress={() => setShowCategoryModal(true)}>")
    def replacer_goal(m):
        return m.group(0).replace("<TouchableOpacity style={styles.dropdownBox}>", "<TouchableOpacity style={styles.dropdownBox} onPress={() => setShowGoalModal(true)}>")
    def replacer_reminder(m):
        return m.group(0).replace("<TouchableOpacity style={styles.dropdownBox}>", "<TouchableOpacity style={styles.dropdownBox} onPress={() => setShowReminderModal(true)}>")
    def replacer_repeat(m):
        return m.group(0).replace("<TouchableOpacity style={styles.dropdownBox}>", "<TouchableOpacity style={styles.dropdownBox} onPress={() => setShowRepeatModal(true)}>")

    content = re.sub(r"<Text style=\{styles\.fieldLabel\}>Category</Text>\s*<TouchableOpacity style=\{styles\.dropdownBox\}>", replacer_category, content, flags=re.DOTALL)
    content = re.sub(r"<Text style=\{styles\.fieldLabelLight\}>Add to Goal.*?</Text>\s*<TouchableOpacity style=\{styles\.dropdownBox\}>", replacer_goal, content, flags=re.DOTALL)
    content = re.sub(r"<Text style=\{styles\.fieldLabel\}>Set Reminder</Text>\s*<TouchableOpacity style=\{styles\.dropdownBox\}>", replacer_reminder, content, flags=re.DOTALL)
    content = re.sub(r"<Text style=\{styles\.fieldLabel\}>Repeat</Text>\s*<TouchableOpacity style=\{styles\.dropdownBox\}>", replacer_repeat, content, flags=re.DOTALL)

    # 8. Replace Subtasks section (only in AddTaskScreen)
    if "Add Subtasks" in content:
        content = re.sub(
            r"<Text style=\{styles\.fieldLabel\}>Add Subtasks</Text>[\s\S]*?(?=\{\/\* Add Attachments \*\/)",
            r"<Text style={styles.fieldLabel}>Add Subtasks</Text>\n          <SubtaskManager subtasks={subtasks} onChange={setSubtasks} />\n\n          ",
            content
        )

    # 9. Replace Attachments section
    attachment_code = """
          {/* Add Attachments */}
          <Text style={styles.fieldLabelLight}>
            Add Attachments <Text style={styles.optionalText}>(optional)</Text>
          </Text>
          <AttachmentPicker attachments={attachments} onChange={setAttachments} />
"""
    if "Add Attachments" in content:
        content = re.sub(
            r"\{\/\* Add Attachments \*\/\}[\s\S]*?</TouchableOpacity>",
            attachment_code.strip(),
            content
        )
    else:
        # If it doesn't exist, inject it right before the final spacer or </ScrollView>
        if "<View style={{ height: 100 }} />" in content:
            content = content.replace("<View style={{ height: 100 }} />", attachment_code + "\n          <View style={{ height: 100 }} />")
        elif "<!-- Action Buttons -->" in content or "{/* Action Buttons */}" in content:
            content = content.replace("{/* Action Buttons */}", attachment_code + "\n          {/* Action Buttons */}")
        else:
            # Fallback
            pass

    # 10. Add SelectionModal components
    modals = """
        <SelectionModal visible={showCategoryModal} onClose={() => setShowCategoryModal(false)} title="Select Category" options={['Work', 'Personal', 'Health']} selectedOption={selectedCategory} onSelect={setSelectedCategory} />
        <SelectionModal visible={showGoalModal} onClose={() => setShowGoalModal(false)} title="Select Goal" options={['Build a strong portfolio', 'Learn React Native', 'None']} selectedOption={selectedGoal} onSelect={setSelectedGoal} />
        <SelectionModal visible={showReminderModal} onClose={() => setShowReminderModal(false)} title="Set Reminder" options={['None', '5 Min', '10 Min', '30 Min', '1 Hour']} selectedOption={reminder} onSelect={setReminder} />
        <SelectionModal visible={showRepeatModal} onClose={() => setShowRepeatModal(false)} title="Repeat" options={['Does not repeat', 'Daily', 'Weekly', 'Monthly']} selectedOption={repeat} onSelect={setRepeat} />
"""
    if "SelectionModal visible={showCategoryModal}" not in content:
        if "</ScrollView>" in content:
            content = content.replace("</ScrollView>", modals + "\n        </ScrollView>")

    # 11. Pass attachments to the hook save function
    if "const data = {" in content:
        content = re.sub(
            r"(const data = \{)([\s\S]*?)(\s*};)",
            lambda m: m.group(1) + m.group(2) + ("  attachments,\n" if "attachments" not in m.group(2) else "") + m.group(3),
            content,
            count=1
        )
    elif "Data = {" in content:
        content = re.sub(
            r"(const \w+Data = \{)([\s\S]*?)(\s*};)",
            lambda m: m.group(1) + m.group(2) + ("  attachments,\n" if "attachments" not in m.group(2) else "") + m.group(3),
            content,
            count=1
        )

    # Some screens (like AddHabitScreen) use undefined states for Category/Goal/Reminder/Repeat, 
    # but since they don't have those touchables, we don't have to worry about them breaking anything.
    # We added the modal states just in case as requested.

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for screen in screens:
    process_file(screen)
    print(f"Processed {screen}")
