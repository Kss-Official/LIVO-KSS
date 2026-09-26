import re

with open('c:/Users/HP/OneDrive/Desktop/Livo/frontend/src/screens/InsightsScreen.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add useEvents import
content = content.replace("import { useGoals } from '../hooks/useGoals';", "import { useGoals } from '../hooks/useGoals';\nimport { useEvents } from '../hooks/useEvents';")

# 2. Find where hooks are called
hooks_search = """  const { tasks } = useTasks();
  const { habits } = useHabits();
  const { goals } = useGoals();"""

hooks_replace = """  const { tasks } = useTasks();
  const { habits } = useHabits();
  const { goals } = useGoals();
  const { events } = useEvents();

  const getFilteredData = <T extends { date?: string; dueDate?: string; targetDate?: string; createdAt?: string }>(data: T[], timeframe: string) => {
    return data.filter(item => {
      if (timeframe === 'Overall') return true;
      const dateStr = item.date || item.dueDate || item.targetDate || item.createdAt;
      if (!dateStr) return false;
      
      const date = new Date(dateStr);
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

      if (timeframe === 'This Month') {
        return date.getFullYear() === today.getFullYear() && date.getMonth() === today.getMonth();
      }

      if (timeframe === 'This Week') {
        const day = today.getDay();
        const diff = today.getDate() - day + (day === 0 ? -6 : 1);
        const start = new Date(today);
        start.setDate(diff);
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        end.setHours(23, 59, 59, 999);
        return date >= start && date <= end;
      }

      if (timeframe === 'Last Week') {
        const day = today.getDay();
        const diff = today.getDate() - day + (day === 0 ? -6 : 1);
        const startThis = new Date(today);
        startThis.setDate(diff);
        const start = new Date(startThis);
        start.setDate(startThis.getDate() - 7);
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        end.setHours(23, 59, 59, 999);
        return date >= start && date <= end;
      }

      return true;
    });
  };

  const filteredTasks = getFilteredData(tasks, selectedTimeframe);
  const filteredHabits = getFilteredData(habits, selectedTimeframe);
  const filteredGoals = getFilteredData(goals, selectedTimeframe);
  const filteredEvents = getFilteredData(events, selectedTimeframe);"""

content = content.replace(hooks_search, hooks_replace)

# 3. Replace real variables
vars_search = """  const realTotalTasks = tasks.length || 10;
  const realCompletedTasks = tasks.filter(t => t.completed).length || 8;
  const realTotalHabits = habits.length || 7;
  const realDoneHabits = habits.filter(h => h.streakCount > 0).length || 5;
  const realTotalGoals = goals.length || 3;
  const realProgressingGoals = goals.filter(g => (g.progressPercentage || 0) > 0).length || 2;"""

vars_replace = """  const realTotalTasks = filteredTasks.length;
  const realCompletedTasks = filteredTasks.filter(t => t.completed).length;
  const realTotalHabits = filteredHabits.length;
  const realDoneHabits = filteredHabits.filter(h => h.streakCount > 0 || h.completedToday).length;
  const realTotalGoals = filteredGoals.length;
  const realProgressingGoals = filteredGoals.filter(g => (g.progressPercentage || 0) > 0).length;"""

content = content.replace(vars_search, vars_replace)

# 4. Replace insightsList
insights_search = """  const insightsList = [
    "You're more productive on days when you work out. Consider keeping your morning workouts!",
    "You complete 40% more tasks when you set high priority items before 10 AM.",
    "Your learning streak is up 25% compared to last week. Great job staying consistent!",
  ];"""

insights_replace = """  const insightsList = [
    `You completed ${realCompletedTasks} tasks for ${selectedTimeframe}! Keep up the momentum.`,
    `You have ${realDoneHabits} active habits right now. Consistency is key!`,
    `You're making progress on ${realProgressingGoals} goals. Keep focusing on what matters.`,
  ];"""

content = content.replace(insights_search, insights_replace)

# 5. Replace legendData
legend_search = """  const legendData = [
    { label: 'Work', percent: '40%', color: '#66C400' },
    { label: 'Personal', percent: '20%', color: '#3B82F6' },
    { label: 'Learning', percent: '15%', color: '#8B5CF6' },
    { label: 'Health', percent: '10%', color: '#EF4444' },
    { label: 'Travel', percent: '5%', color: '#F97316' },
    { label: 'Others', percent: '10%', color: '#CBD5E1' },
  ];"""

legend_replace = """  let totalMinutes = 0;
  const categoryCounts: Record<string, number> = { Work: 0, Personal: 0, Learning: 0, Health: 0, Travel: 0, Others: 0 };
  
  const processItem = (category?: string, duration?: string | number) => {
    let cat = category || 'Others';
    const matchCat = ['Work', 'Personal', 'Learning', 'Health', 'Travel', 'Others'].find(c => c.toLowerCase() === cat.toLowerCase());
    cat = matchCat || 'Others';
    let mins = 30; // default 30 mins
    if (typeof duration === 'number') mins = duration;
    else if (typeof duration === 'string') {
      const num = parseInt(duration);
      if (!isNaN(num)) mins = num;
    }
    categoryCounts[cat] += mins;
    totalMinutes += mins;
  };

  filteredTasks.forEach(t => processItem(t.category, t.estimatedMinutes || t.duration));
  filteredEvents.forEach(e => {
    let mins = 60; // default
    if (e.startTime && e.endTime) {
      const startMatch = e.startTime.match(/(\d+):(\d+)\s*(AM|PM)?/i);
      const endMatch = e.endTime.match(/(\d+):(\d+)\s*(AM|PM)?/i);
      if (startMatch && endMatch) {
         let sh = parseInt(startMatch[1]), sm = parseInt(startMatch[2]);
         if (startMatch[3]?.toUpperCase() === 'PM' && sh < 12) sh += 12;
         if (startMatch[3]?.toUpperCase() === 'AM' && sh === 12) sh = 0;
         let eh = parseInt(endMatch[1]), em = parseInt(endMatch[2]);
         if (endMatch[3]?.toUpperCase() === 'PM' && eh < 12) eh += 12;
         if (endMatch[3]?.toUpperCase() === 'AM' && eh === 12) eh = 0;
         const diff = (eh * 60 + em) - (sh * 60 + sm);
         if (diff > 0) mins = diff;
      }
    }
    processItem(e.category, mins);
  });

  const colors: Record<string, string> = {
    Work: '#66C400', Personal: '#3B82F6', Learning: '#8B5CF6', 
    Health: '#EF4444', Travel: '#F97316', Others: '#CBD5E1'
  };

  const legendData = Object.entries(categoryCounts).map(([label, score]) => {
    const percent = totalMinutes > 0 ? Math.round((score / totalMinutes) * 100) : (label === 'Others' ? 100 : 0);
    return { label, percent: percent + '%', color: colors[label] || '#CBD5E1', score };
  }).sort((a, b) => b.score - a.score);

  const totalTimeStr = totalMinutes > 0 ? `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m` : '0h 0m';
  const totalTimeHours = totalMinutes > 0 ? Math.floor(totalMinutes / 60) : 0;
  const totalTimeMins = totalMinutes > 0 ? totalMinutes % 60 : 0;"""

content = content.replace(legend_search, legend_replace)

# 6. Replace productiveHours
prod_search = """  const productiveHours: { label: string; height: DimensionValue; color: string }[] = [
    { label: '6am', height: '40%', color: '#EBF9DB' },
    { label: '9am', height: '90%', color: '#66C400' },
    { label: '12pm', height: '80%', color: '#66C400' },
    { label: '3pm', height: '35%', color: '#EBF9DB' },
    { label: '6pm', height: '55%', color: '#EBF9DB' },
    { label: '9pm', height: '30%', color: '#EBF9DB' },
  ];"""

prod_replace = """  const hourBuckets = { '6am': 0, '9am': 0, '12pm': 0, '3pm': 0, '6pm': 0, '9pm': 0 };
  const parseHour = (timeStr?: string) => {
    if (!timeStr) return null;
    const match = timeStr.match(/(\d+):(\d+)\s*(AM|PM)/i);
    if (match) {
        let h = parseInt(match[1]);
        if (match[3].toUpperCase() === 'PM' && h < 12) h += 12;
        if (match[3].toUpperCase() === 'AM' && h === 12) h = 0;
        return h;
    }
    const match24 = timeStr.match(/(\d+):(\d+)/);
    if (match24) return parseInt(match24[1]);
    return null;
  };
  const processTime = (timeStr?: string) => {
    const h = parseHour(timeStr);
    if (h === null) return;
    if (h >= 5 && h < 9) hourBuckets['6am']++;
    else if (h >= 9 && h < 12) hourBuckets['9am']++;
    else if (h >= 12 && h < 15) hourBuckets['12pm']++;
    else if (h >= 15 && h < 18) hourBuckets['3pm']++;
    else if (h >= 18 && h < 21) hourBuckets['6pm']++;
    else hourBuckets['9pm']++;
  };
  filteredTasks.forEach(t => processTime(t.time));
  filteredEvents.forEach(e => processTime(e.startTime));
  
  const maxBucket = Math.max(...Object.values(hourBuckets), 1);
  const productiveHours: { label: string; height: DimensionValue; color: string }[] = [
    { label: '6am', height: `${(hourBuckets['6am'] / maxBucket) * 100}%`, color: hourBuckets['6am'] === maxBucket && hourBuckets['6am'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '9am', height: `${(hourBuckets['9am'] / maxBucket) * 100}%`, color: hourBuckets['9am'] === maxBucket && hourBuckets['9am'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '12pm', height: `${(hourBuckets['12pm'] / maxBucket) * 100}%`, color: hourBuckets['12pm'] === maxBucket && hourBuckets['12pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '3pm', height: `${(hourBuckets['3pm'] / maxBucket) * 100}%`, color: hourBuckets['3pm'] === maxBucket && hourBuckets['3pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '6pm', height: `${(hourBuckets['6pm'] / maxBucket) * 100}%`, color: hourBuckets['6pm'] === maxBucket && hourBuckets['6pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '9pm', height: `${(hourBuckets['9pm'] / maxBucket) * 100}%`, color: hourBuckets['9pm'] === maxBucket && hourBuckets['9pm'] > 0 ? '#66C400' : '#EBF9DB' },
  ];
  
  let bestTime = 'morning';
  if (hourBuckets['12pm'] + hourBuckets['3pm'] > hourBuckets['6am'] + hourBuckets['9am']) bestTime = 'afternoon';
  if (hourBuckets['6pm'] + hourBuckets['9pm'] > Math.max(hourBuckets['6am'] + hourBuckets['9am'], hourBuckets['12pm'] + hourBuckets['3pm'])) bestTime = 'evening';
  if (maxBucket === 1 && Object.values(hourBuckets).every(v => v === 0)) bestTime = 'any time';"""

content = content.replace(prod_search, prod_replace)

# 7. Replace habitDays
habit_search = """  const habitDays = [
    { day: 'Mon', completed: true },
    { day: 'Tue', completed: true },
    { day: 'Wed', completed: true },
    { day: 'Thu', completed: false },
    { day: 'Fri', completed: true },
    { day: 'Sat', completed: true },
    { day: 'Sun', completed: false },
  ];"""

habit_replace = """  const habitDays = Array.from({ length: 7 }).map((_, i) => {
    const d = 6 - i;
    const date = new Date();
    date.setDate(date.getDate() - d);
    const dayName = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][date.getDay()];
    const completed = habits.some(h => {
      if (d === 0) return h.completedToday;
      if (h.completedToday) return d < h.streakCount;
      return d <= h.streakCount && d > 0;
    });
    return { day: dayName, completed };
  });"""

content = content.replace(habit_search, habit_replace)

# 8. Replace goalsList
goals_search = """  const goalsList = [
    { title: 'Build a strong port', progress: 60 },
    { title: 'Learn React', progress: 40 },
    { title: 'Improve fitness', progress: 75 },
  ];"""

goals_replace = """  const goalsList = filteredGoals.slice(0, 3).map(g => ({
    title: g.title,
    progress: Math.round(g.progressPercentage || 0)
  }));
  if (goalsList.length === 0) {
    goalsList.push({ title: 'No goals set for this timeframe', progress: 0 });
  }"""

content = content.replace(goals_search, goals_replace)


# 9. Update render texts
render_search_1 = "<Text style={styles.totalTimeSub}>Total 28h 30m</Text>"
render_replace_1 = "<Text style={styles.totalTimeSub}>Total {totalTimeStr}</Text>"
content = content.replace(render_search_1, render_replace_1)

render_search_2 = """<View style={styles.donutInnerCircle}>
                  <Text style={styles.donutCenterValue}>28h</Text>
                  <Text style={styles.donutCenterSub}>30m</Text>
                </View>"""
render_replace_2 = """<View style={styles.donutInnerCircle}>
                  <Text style={styles.donutCenterValue}>{totalTimeHours}h</Text>
                  <Text style={styles.donutCenterSub}>{totalTimeMins}m</Text>
                </View>"""
content = content.replace(render_search_2, render_replace_2)

render_search_3 = "<Text style={styles.hintSubText}>You do your best work in the morning.</Text>"
render_replace_3 = "<Text style={styles.hintSubText}>You do your best work in the {bestTime}.</Text>"
content = content.replace(render_search_3, render_replace_3)


with open('c:/Users/HP/OneDrive/Desktop/Livo/frontend/src/screens/InsightsScreen.tsx', 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
