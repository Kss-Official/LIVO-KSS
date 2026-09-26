import os
import re

screens = [
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddTaskScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddEventScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddGoalScreen.tsx",
    r"c:\Users\HP\OneDrive\Desktop\Livo\frontend\src\screens\AddHabitScreen.tsx"
]

def patch_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix SelectionModal options and selectedValue
    content = content.replace("options={['Work', 'Personal', 'Health']}", "options={[{label: 'Work', value: 'Work'}, {label: 'Personal', value: 'Personal'}, {label: 'Health', value: 'Health'}]}")
    content = content.replace("options={['Build a strong portfolio', 'Learn React Native', 'None']}", "options={[{label: 'Build a strong portfolio', value: 'Build a strong portfolio'}, {label: 'Learn React Native', value: 'Learn React Native'}, {label: 'None', value: 'None'}]}")
    content = content.replace("options={['None', '5 Min', '10 Min', '30 Min', '1 Hour']}", "options={[{label: 'None', value: 'None'}, {label: '5 Min', value: '5 Min'}, {label: '10 Min', value: '10 Min'}, {label: '30 Min', value: '30 Min'}, {label: '1 Hour', value: '1 Hour'}]}")
    content = content.replace("options={['Does not repeat', 'Daily', 'Weekly', 'Monthly']}", "options={[{label: 'Does not repeat', value: 'Does not repeat'}, {label: 'Daily', value: 'Daily'}, {label: 'Weekly', value: 'Weekly'}, {label: 'Monthly', value: 'Monthly'}]}")

    content = content.replace("selectedOption={", "selectedValue={")

    # Add missing state variables if they are missing
    missing_states = []
    if "const [selectedCategory" not in content:
        missing_states.append("const [selectedCategory, setSelectedCategory] = useState('Work');")
    if "const [selectedGoal" not in content:
        missing_states.append("const [selectedGoal, setSelectedGoal] = useState('Build a strong portfolio');")
    if "const [reminder," not in content:
        missing_states.append("const [reminder, setReminder] = useState('30 Min');")
    if "const [repeat," not in content:
        missing_states.append("const [repeat, setRepeat] = useState('Does not repeat');")
    
    if missing_states:
        # insert after showRepeatModal
        insert_str = "\n  " + "\n  ".join(missing_states)
        content = re.sub(
            r"(const \[showRepeatModal, setShowRepeatModal\] = useState\(false\);)",
            r"\1" + insert_str,
            content
        )

    # Fix fieldLabelLight for screens that don't have it
    if "fieldLabelLight:" not in content and "fieldLabelLight" in content:
        content = content.replace("styles.fieldLabelLight", "styles.label")
        # For AddTaskScreen, fieldLabelLight does exist, but AddTaskScreen uses styles.fieldLabelLight. Let's make sure it doesn't break AddTaskScreen.
        # Wait, if "fieldLabelLight:" is in AddTaskScreen, it won't be replaced. 

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for screen in screens:
    patch_file(screen)
