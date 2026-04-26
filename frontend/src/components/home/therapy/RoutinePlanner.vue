<template>
  <div class="routine-shell">
    <div class="float-deco d1">📋</div>
    <div class="float-deco d2">⭐</div>
    <div class="float-deco d3">🌈</div>
    <div class="float-deco d4">🎯</div>

    <header class="rp-header">
      <h1>📅 My Daily Routine</h1>
      <p>Build a calm and happy daily plan!</p>
    </header>

    <!-- Suggested templates -->
    <div class="template-row">
      <span class="tmpl-label">Quick Add:</span>
      <button v-for="t in templates" :key="t" class="tmpl-chip" @click="addFromTemplate(t)">{{ t }}</button>
    </div>

    <!-- Add custom task -->
    <div class="add-row">
      <div class="input-wrap">
        <span class="input-icon">✏️</span>
        <input v-model="task" placeholder="Add a new task..." @keyup.enter="addTask" maxlength="80">
      </div>
      <button class="btn-add" @click="addTask">➕ Add</button>
    </div>

    <!-- Task list -->
    <div class="task-list" v-if="tasks.length">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
        <span class="progress-text">{{ doneCount }}/{{ tasks.length }} done {{ progressEmoji }}</span>
      </div>

      <transition-group name="task" tag="div" class="task-items">
        <div v-for="(item, i) in tasks" :key="item.id" class="task-card" :class="{ completed: item.done }">
          <button class="check-btn" @click="toggleDone(i)">
            <span v-if="item.done">✅</span>
            <span v-else>⬜</span>
          </button>
          <div class="task-info">
            <span class="task-label" :class="{ struck: item.done }">{{ item.label }}</span>
            <span class="task-time" v-if="item.time">🕐 {{ item.time }}</span>
          </div>
          <button class="remove-btn" @click="removeTask(i)">🗑️</button>
        </div>
      </transition-group>
    </div>

    <div v-else class="empty-state">
      <div class="empty-emoji">📝</div>
      <p>No tasks yet! Add your first routine task above.</p>
    </div>

    <!-- Actions -->
    <div class="action-row">
      <button class="btn-save" @click="save">💾 Save Routine</button>
      <button class="btn-clear" @click="clearAll" v-if="tasks.length">🧹 Clear All</button>
    </div>

    <transition name="fade">
      <div class="save-toast" v-if="showToast">✅ Routine Saved!</div>
    </transition>

    <!-- Celebration -->
    <div class="celebrate" v-if="allDone && tasks.length >= 3">
      <div class="celeb-card">
        <div class="celeb-emoji">🎉🌟🏆</div>
        <p>Amazing! You finished ALL your tasks today!</p>
      </div>
    </div>
  </div>
</template>

<script>
let nextId = Date.now();
export default {
  name: 'RoutinePlanner',
  data() {
    const saved = JSON.parse(localStorage.getItem('routine_planner_tasks') || '[]');
    return {
      task: '',
      tasks: saved.map(t => ({ ...t, id: t.id || nextId++ })),
      showToast: false,
      templates: [
        '🌅 Morning Breathing', '🪥 Brush Teeth', '🥣 Breakfast Time',
        '📚 Story Time', '🎨 Art Activity', '🧘 Calm Break',
        '🍎 Snack Time', '🛁 Bath Time', '🌙 Bedtime Routine',
      ],
    };
  },
  computed: {
    doneCount() { return this.tasks.filter(t => t.done).length; },
    progressPct() { return this.tasks.length ? Math.round((this.doneCount / this.tasks.length) * 100) : 0; },
    allDone() { return this.tasks.length > 0 && this.doneCount === this.tasks.length; },
    progressEmoji() {
      const p = this.progressPct;
      if (p === 100) return '🏆';
      if (p >= 75) return '🔥';
      if (p >= 50) return '💪';
      if (p >= 25) return '👍';
      return '🌱';
    },
  },
  methods: {
    addTask() {
      if (!this.task.trim()) return;
      this.tasks.push({ id: nextId++, label: this.task.trim(), done: false, time: '' });
      this.task = '';
      this.autoSave();
    },
    addFromTemplate(tmpl) {
      if (this.tasks.some(t => t.label === tmpl)) return;
      this.tasks.push({ id: nextId++, label: tmpl, done: false, time: '' });
      this.autoSave();
    },
    toggleDone(i) { this.tasks[i].done = !this.tasks[i].done; this.autoSave(); },
    removeTask(i) { this.tasks.splice(i, 1); this.autoSave(); },
    clearAll() { this.tasks = []; this.autoSave(); },
    save() {
      localStorage.setItem('routine_planner_tasks', JSON.stringify(this.tasks));
      this.showToast = true;
      setTimeout(() => { this.showToast = false; }, 2000);
    },
    autoSave() { localStorage.setItem('routine_planner_tasks', JSON.stringify(this.tasks)); },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.routine-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #EDE9FE 0%, #FFF7ED 50%, #E0F2FE 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 6%; right: 6%; } .d2 { top: 25%; left: 4%; animation-delay: 1.5s; }
.d3 { bottom: 15%; right: 5%; animation-delay: 3s; } .d4 { bottom: 30%; left: 6%; animation-delay: 0.8s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.rp-header { text-align: center; margin-bottom: 16px; }
.rp-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.rp-header p { margin: 4px 0 0; color: #5A4690; }

/* Templates */
.template-row { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; margin-bottom: 14px; }
.tmpl-label { font-weight: 700; color: #5A4690; font-size: 0.85rem; margin-right: 4px; }
.tmpl-chip {
  background: rgba(255,255,255,0.8); border: 2px solid #D4CCFF; border-radius: 50px;
  padding: 5px 12px; font-family: 'Syne', sans-serif; font-weight: 600;
  font-size: 0.78rem; color: #5A4690; cursor: pointer; transition: all 0.2s;
}
.tmpl-chip:hover { background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; border-color: transparent; }

/* Add row */
.add-row { display: flex; gap: 8px; margin-bottom: 16px; }
.input-wrap {
  flex: 1; display: flex; align-items: center; background: rgba(255,255,255,0.9);
  border: 2px solid #D4CCFF; border-radius: 14px; padding: 0 12px; backdrop-filter: blur(6px);
}
.input-icon { font-size: 1.1rem; margin-right: 8px; }
.input-wrap input {
  flex: 1; border: none; outline: none; background: transparent; padding: 12px 0;
  font-family: 'Syne', sans-serif; font-size: 0.95rem; color: #1A0B3E;
}
.btn-add {
  border: none; border-radius: 14px; padding: 12px 20px; font-family: 'Syne', sans-serif;
  font-weight: 700; background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff;
  cursor: pointer; transition: all 0.25s;
}
.btn-add:hover { transform: translateY(-2px); }

/* Progress */
.progress-bar {
  position: relative; height: 28px; background: rgba(255,255,255,0.7);
  border-radius: 50px; border: 2px solid #D4CCFF; margin-bottom: 12px; overflow: hidden;
}
.progress-fill {
  position: absolute; left: 0; top: 0; height: 100%; border-radius: 50px;
  background: linear-gradient(90deg, #6C63FF, #A855F7, #22c55e); transition: width 0.5s ease;
}
.progress-text {
  position: relative; z-index: 1; display: flex; align-items: center; justify-content: center;
  height: 100%; font-weight: 700; font-size: 0.8rem; color: #1A0B3E;
}

/* Tasks */
.task-items { display: flex; flex-direction: column; gap: 8px; }
.task-card {
  display: flex; align-items: center; gap: 10px; padding: 12px 14px;
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF;
  border-radius: 14px; backdrop-filter: blur(6px); transition: all 0.3s;
}
.task-card.completed { border-color: #86EFAC; background: rgba(134,239,172,0.15); }
.check-btn { background: none; border: none; font-size: 1.3rem; cursor: pointer; padding: 0; line-height: 1; }
.task-info { flex: 1; }
.task-label { font-weight: 600; color: #1A0B3E; }
.task-label.struck { text-decoration: line-through; color: #94a3b8; }
.task-time { font-size: 0.78rem; color: #8B7EC8; margin-left: 8px; }
.remove-btn { background: none; border: none; font-size: 1.1rem; cursor: pointer; opacity: 0.5; transition: opacity 0.2s; }
.remove-btn:hover { opacity: 1; }

.task-enter-active, .task-leave-active { transition: all 0.3s ease; }
.task-enter-from { opacity: 0; transform: translateX(-20px); }
.task-leave-to { opacity: 0; transform: translateX(20px); }

/* Empty */
.empty-state { text-align: center; padding: 40px 0; color: #8B7EC8; }
.empty-emoji { font-size: 3rem; margin-bottom: 8px; }

/* Actions */
.action-row { display: flex; gap: 10px; justify-content: center; margin-top: 18px; }
.btn-save, .btn-clear {
  border: none; border-radius: 50px; padding: 12px 28px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; transition: all 0.25s;
}
.btn-save { background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; box-shadow: 0 4px 16px rgba(108,99,255,0.2); }
.btn-save:hover { transform: translateY(-2px); }
.btn-clear { background: rgba(255,255,255,0.8); border: 2px solid #FCA5A5; color: #DC2626; }

/* Toast */
.save-toast {
  position: fixed; bottom: 30px; left: 50%; transform: translateX(-50%);
  background: linear-gradient(135deg,#22c55e,#16a34a); color: #fff;
  padding: 12px 28px; border-radius: 50px; font-weight: 700; font-family: 'Syne', sans-serif;
  box-shadow: 0 6px 20px rgba(34,197,94,0.3); z-index: 999;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Celebration */
.celebrate { display: flex; justify-content: center; margin-top: 16px; }
.celeb-card {
  background: rgba(255,255,255,0.9); backdrop-filter: blur(8px);
  border: 2px solid #FCD34D; border-radius: 20px; padding: 20px 30px;
  text-align: center; animation: cardIn 0.5s ease;
}
@keyframes cardIn { from{opacity:0;transform:scale(0.9)} to{opacity:1;transform:scale(1)} }
.celeb-emoji { font-size: 2.5rem; }
.celeb-card p { color: #3D2B7A; font-weight: 700; margin: 8px 0 0; }
</style>
