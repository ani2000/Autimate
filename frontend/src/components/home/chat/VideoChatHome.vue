<template>
    <section class="call-shell">
        <div class="bg-orb orb-a"></div>
        <div class="bg-orb orb-b"></div>

        <header class="call-header">
            <div>
                <p class="eyebrow">Social Communication Studio</p>
                <h2>Therapy Video Session</h2>
                <p class="subtitle">
                    Structured live practice for turn-taking, eye contact, and expressive communication.
                </p>
            </div>
            <div class="mode-pill">
                {{ selectedActionMode.label }}
                <span>{{ participantCountText }}</span>
            </div>
        </header>

        <p v-if="loading" class="status status-info">Connecting to room...</p>
        <p v-if="notice" class="status status-warn">{{ notice }}</p>
        <p v-if="error" class="status status-error">{{ error }}</p>

        <div class="prejoin-card" v-if="!joined">
            <h3>Choose What You Want To Do</h3>
            <div class="grid-2">
                <label class="field">
                    <span>Your Name</span>
                    <input v-model="displayName" type="text" placeholder="e.g. Ayan" class="input" maxlength="28" />
                </label>
                <label class="field">
                    <span>Your Role</span>
                    <select v-model="participantRole" class="input">
                        <option v-for="role in roleOptions" :key="role.value" :value="role.value">{{ role.label }}</option>
                    </select>
                </label>
            </div>

            <label class="field">
                <span>Your User ID</span>
                <input v-model="callerUserId" placeholder="e.g. child_ayon_01" class="input" />
            </label>

            <div class="mode-grid access-grid">
                <button
                    v-for="mode in actionModes"
                    :key="mode.id"
                    type="button"
                    class="mode-card"
                    :class="{ active: actionMode === mode.id }"
                    @click="actionMode = mode.id"
                >
                    <strong>{{ mode.label }}</strong>
                    <p>{{ mode.description }}</p>
                </button>
            </div>

            <label class="field">
                <span v-if="actionMode === 'create-call'">New Session ID</span>
                <span v-else-if="actionMode === 'join-call'">Existing Session ID</span>
                <span v-else>Live Stream ID</span>
                <input
                    v-if="actionMode === 'create-call'"
                    v-model="createSessionId"
                    @input="onSessionInput('createSessionId')"
                    inputmode="numeric"
                    maxlength="4"
                    placeholder="e.g. 7731"
                    class="input"
                />
                <input
                    v-if="actionMode === 'join-call'"
                    v-model="joinSessionId"
                    @input="onSessionInput('joinSessionId')"
                    inputmode="numeric"
                    maxlength="4"
                    placeholder="Enter 4-digit code"
                    class="input"
                />
                <input
                    v-if="actionMode === 'watch-live'"
                    v-model="liveStreamId"
                    @input="onSessionInput('liveStreamId')"
                    inputmode="numeric"
                    maxlength="4"
                    placeholder="e.g. 7731"
                    class="input"
                />
            </label>

            <label class="field" v-if="actionMode === 'create-call'">
                <span>Create As</span>
                <select v-model="createChannelType" class="input">
                    <option value="call">Regular Call</option>
                    <option value="live">Live Stream Host</option>
                </select>
            </label>

            <button class="btn" type="button" @click="generateSessionId" v-if="actionMode === 'create-call'">
                Generate Session ID
            </button>

            <p class="goal-note access-note">Session Prefix: session-{{ resolvedSessionCode || '----' }}</p>
            <p class="goal-note access-note">Final Channel: {{ resolvedChannelName || '-' }}</p>
            <p class="goal-note">Goal: support children with special needs through predictable, comfortable social practice.</p>

            <button class="btn btn-primary" @click="joinCall" :disabled="loading || !canJoin">
                {{ actionButtonText }}
            </button>
        </div>

        <div v-if="joined" class="session-wrap">
            <div class="toolbar">
                <div class="session-facts">
                    <p><strong>Room:</strong> {{ activeChannelName || channelName }}</p>
                    <p><strong>Mode:</strong> {{ selectedActionMode.label }}</p>
                    <p><strong>Participants:</strong> {{ participantCountText }}</p>
                </div>
                <div class="actions">
                    <button class="btn" @click="toggleMic" :disabled="!localAudioTrack">
                        {{ localAudioTrack ? (micEnabled ? 'Mute Mic' : 'Unmute Mic') : 'Mic Unavailable' }}
                    </button>
                    <button class="btn" @click="toggleCam" :disabled="!localVideoTrack">
                        {{ localVideoTrack ? (camEnabled ? 'Turn Camera Off' : 'Turn Camera On') : 'Camera Unavailable' }}
                    </button>
                    <button class="btn btn-danger" @click="leaveCall">Leave</button>
                </div>
            </div>

            <div class="social-hint">
                <strong>Session Focus:</strong> {{ selectedActionMode.coachingHint }}
            </div>

            <div class="video-grid">
                <article class="participant local">
                    <div class="participant-badge">You • {{ roleLabel(participantRole) }}</div>
                    <div class="participant-name">{{ displayName || 'Participant' }} • @{{ callerUserId || 'you' }}</div>
                    <div id="local-player" class="video-player"></div>
                    <div class="media-status" v-if="!localVideoTrack || !camEnabled">
                        <span>Camera Off</span>
                    </div>
                </article>

                <article class="participant" v-for="user in remoteUsers" :key="String(user.uid)">
                    <div class="participant-badge">{{ remoteRoleLabel(user.uid) }}</div>
                    <div class="participant-name">{{ remoteNameLabel(user.uid) }} • @{{ remoteUserKey(user.uid) }}</div>
                    <div :id="remotePlayerId(user.uid)" class="video-player"></div>
                    <div class="media-status" v-if="!user.hasVideo">
                        <span>Camera Off</span>
                    </div>
                </article>
            </div>
        </div>
    </section>
</template>

<script>
import AgoraRTC from 'agora-rtc-sdk-ng';

export default {
    name: 'VideoChatHome',
    data() {
        return {
            appId: 'dfec6c69c6cc430bb7ecb5155888977b',
            token: null,
            channelName: this.$route?.query?.roomID || 'social-room',
            activeChannelName: '',
            client: null,
            localUid: null,
            localAudioTrack: null,
            localVideoTrack: null,
            remoteUsers: [],
            joined: false,
            loading: false,
            error: '',
            notice: '',
            lastSubscriptionError: '',
            micEnabled: true,
            camEnabled: true,
            displayName: localStorage.getItem('therapy_display_name') || '',
            participantRole: localStorage.getItem('therapy_participant_role') || 'child',
            callerUserId: localStorage.getItem('therapy_user_id') || '',
            actionMode: 'create-call',
            createSessionId: this.$route?.query?.roomID || '',
            joinSessionId: this.$route?.query?.roomID || '',
            liveStreamId: this.$route?.query?.roomID || '',
            createChannelType: 'call',
            roleOptions: [
                { value: 'child', label: 'Child' },
                { value: 'therapist', label: 'Therapist' },
                { value: 'parent', label: 'Parent' },
                { value: 'teacher', label: 'Teacher' },
                { value: 'caregiver', label: 'Caregiver' },
            ],
            actionModes: [
                {
                    id: 'create-call',
                    label: 'Create New Call',
                    maxParticipants: 2,
                    description: 'Start a new call and share Session ID with others.',
                    coachingHint: 'Use calm prompts and pause for response after each sentence.',
                },
                {
                    id: 'join-call',
                    label: 'Join Existing Call',
                    maxParticipants: 10,
                    description: 'Join an existing call using the same Session ID.',
                    coachingHint: 'Call each participant by name to support turn-taking.',
                },
                {
                    id: 'watch-live',
                    label: 'Watch Live Stream',
                    maxParticipants: 100,
                    description: 'Watch a live stream without publishing your own media.',
                    coachingHint: 'Observe social cues and respond with short supportive prompts.',
                },
            ],
        };
    },
    computed: {
        selectedActionMode() {
            return this.actionModes.find(mode => mode.id === this.actionMode) || this.actionModes[0];
        },
        resolvedSessionCode() {
            if (this.actionMode === 'create-call') {
                return this.sanitizeSessionCode(this.createSessionId);
            }
            if (this.actionMode === 'join-call') {
                return this.sanitizeSessionCode(this.joinSessionId);
            }
            return this.sanitizeSessionCode(this.liveStreamId);
        },
        resolvedChannelName() {
            if (this.actionMode === 'create-call') {
                return this.resolveChannel(this.createSessionId, this.createChannelType === 'live' ? 'live' : 'call');
            }
            if (this.actionMode === 'join-call') {
                return this.resolveChannel(this.joinSessionId, 'call');
            }
            return this.resolveChannel(this.liveStreamId, 'live');
        },
        canJoin() {
            const hasName = (this.displayName || '').trim().length > 0;
            const hasCaller = (this.callerUserId || '').trim().length > 0;
            const hasRoom = this.resolvedSessionCode.length === 4 && this.resolvedChannelName.length > 0;

            return hasName && hasCaller && hasRoom;
        },
        actionButtonText() {
            if (this.actionMode === 'create-call') {
                return 'Create And Start';
            }
            if (this.actionMode === 'join-call') {
                return 'Join Call';
            }
            return 'Watch Live';
        },
        participantCountText() {
            if (!this.joined) {
                return `Up to ${this.selectedActionMode.maxParticipants}`;
            }
            return `${1 + this.remoteUsers.length}/${this.selectedActionMode.maxParticipants}`;
        },
    },
    async mounted() {
        // H264 is generally more reliable across mixed desktop/mobile browsers.
        this.client = AgoraRTC.createClient({ mode: 'rtc', codec: 'h264' });
        this.client.on('user-joined', this.handleUserJoined);
        this.client.on('user-published', this.handleUserPublished);
        this.client.on('user-unpublished', this.handleUserUnpublished);
        this.client.on('user-left', this.handleUserLeft);
    },
    async beforeUnmount() {
        await this.leaveCall();
        if (this.client) {
            this.client.off('user-joined', this.handleUserJoined);
            this.client.off('user-published', this.handleUserPublished);
            this.client.off('user-unpublished', this.handleUserUnpublished);
            this.client.off('user-left', this.handleUserLeft);
        }
    },
    methods: {
        roleLabel(roleValue) {
            const role = this.roleOptions.find(item => item.value === roleValue);
            return role ? role.label : 'Participant';
        },
        normalizeSessionInput(value) {
            const cleaned = String(value || '')
                .trim()
                .toLowerCase()
                .replace(/\s+/g, '-')
                .replace(/[^a-z0-9_-]/g, '');

            if (!cleaned) {
                return { key: '', prefix: '', code: '' };
            }

            let prefix = '';
            if (cleaned.startsWith('call-')) {
                prefix = 'call';
            }
            if (cleaned.startsWith('live-')) {
                prefix = 'live';
            }

            const key = cleaned.replace(/^(call-|live-)+/, '');
            const code = this.sanitizeSessionCode(key);
            return { key, prefix, code };
        },
        sanitizeSessionCode(value) {
            return String(value || '')
                .replace(/\D/g, '')
                .slice(0, 4);
        },
        onSessionInput(fieldName) {
            this[fieldName] = this.sanitizeSessionCode(this[fieldName]);
        },
        resolveChannel(value, defaultPrefix) {
            const normalized = this.normalizeSessionInput(value);
            if (normalized.code.length !== 4) {
                return '';
            }
            return `${normalized.prefix || defaultPrefix}-session-${normalized.code}`;
        },
        generateSessionId() {
            this.createSessionId = String(Math.floor(1000 + Math.random() * 9000));
        },
        sanitizeKey(value, fallback) {
            const cleaned = String(value || '')
                .trim()
                .toLowerCase()
                .replace(/[^a-z0-9_-]/g, '-');
            return cleaned || fallback;
        },
        buildUid() {
            const role = (this.participantRole || 'participant').toLowerCase().replace(/[^a-z0-9]/g, '');
            const safeName = (this.displayName || 'guest')
                .trim()
                .toLowerCase()
                .replace(/[^a-z0-9 ]/g, '')
                .replace(/\s+/g, '-');
            const safeUserId = this.sanitizeKey(this.callerUserId, 'user');
            const tag = Math.random().toString(36).slice(2, 5);
            return `${role}__${safeName || 'guest'}__${safeUserId}-${tag}`;
        },
        parseUid(uid) {
            const raw = String(uid || '');
            const parts = raw.split('__');
            if (parts.length < 3) {
                return {
                    role: 'Participant',
                    name: `Participant ${raw.slice(-5) || 'Guest'}`,
                    userKey: `user-${raw.slice(-4) || 'id'}`,
                };
            }

            const rawRole = parts[0];
            const rawName = parts[1];
            const rawUserKey = (parts[2] || '').split('-')[0];
            const role = this.roleLabel(rawRole);
            const name = rawName
                .split('-')
                .filter(Boolean)
                .map(word => word[0].toUpperCase() + word.slice(1))
                .join(' ');

            return {
                role,
                name: name || 'Participant',
                userKey: rawUserKey || 'user',
            };
        },
        remoteNameLabel(uid) {
            return this.parseUid(uid).name;
        },
        remoteRoleLabel(uid) {
            return this.parseUid(uid).role;
        },
        remoteUserKey(uid) {
            return this.parseUid(uid).userKey;
        },
        remotePlayerId(uid) {
            return `remote-player-${String(uid).replace(/[^a-zA-Z0-9_-]/g, '_')}`;
        },
        upsertRemoteUser(user) {
            const userIndex = this.remoteUsers.findIndex(remote => String(remote.uid) === String(user.uid));
            if (userIndex === -1) {
                this.remoteUsers.push(user);
            } else {
                this.remoteUsers.splice(userIndex, 1, user);
            }
        },
        syncRemoteUsersFromClient() {
            if (!this.client) {
                return;
            }
            this.remoteUsers = [...this.client.remoteUsers];
        },
        async subscribeWithRetry(user, mediaType, retries = 4) {
            for (let attempt = 0; attempt <= retries; attempt += 1) {
                try {
                    await this.client.subscribe(user, mediaType);
                    return true;
                } catch (err) {
                    if (attempt === retries) {
                        this.lastSubscriptionError = `${err.code || 'UNKNOWN'}: ${err.message || 'subscription failed'}`;
                        console.error('Failed to subscribe remote user:', err);
                        return false;
                    }
                    await new Promise(resolve => setTimeout(resolve, 450));
                }
            }
            return false;
        },
        async playRemoteTracks(user, mediaType) {
            if (mediaType === 'video' && user.videoTrack) {
                this.$nextTick(() => {
                    user.videoTrack.play(this.remotePlayerId(user.uid), { fit: 'cover' });
                });
            }

            if (mediaType === 'audio' && user.audioTrack) {
                user.audioTrack.play();
            }
        },
        async syncAndSubscribeAll() {
            this.syncRemoteUsersFromClient();
            for (const remote of this.client.remoteUsers) {
                if (remote.hasVideo) {
                    const okVideo = await this.subscribeWithRetry(remote, 'video');
                    if (okVideo) {
                        await this.playRemoteTracks(remote, 'video');
                    }
                }
                if (remote.hasAudio) {
                    const okAudio = await this.subscribeWithRetry(remote, 'audio');
                    if (okAudio) {
                        await this.playRemoteTracks(remote, 'audio');
                    }
                }
            }
        },
        async recoverRemoteMedia(uid, mediaType) {
            const remote = this.client.remoteUsers.find(user => String(user.uid) === String(uid));
            if (!remote) {
                return;
            }
            const ok = await this.subscribeWithRetry(remote, mediaType);
            if (ok) {
                await this.playRemoteTracks(remote, mediaType);
                this.notice = '';
            }
        },
        async joinCall() {
            this.loading = true;
            this.error = '';
            this.notice = '';
            this.lastSubscriptionError = '';

            const name = (this.displayName || '').trim();
            const caller = (this.callerUserId || '').trim();

            this.createSessionId = this.sanitizeSessionCode(this.createSessionId);
            this.joinSessionId = this.sanitizeSessionCode(this.joinSessionId);
            this.liveStreamId = this.sanitizeSessionCode(this.liveStreamId);

            const room = this.resolvedChannelName;

            if (!name || !caller || !room) {
                this.error = 'Please provide your name, user ID, and a 4-digit Session ID.';
                this.loading = false;
                return;
            }

            this.activeChannelName = room;
            this.displayName = name;
            this.callerUserId = caller;
            this.channelName = room;
            localStorage.setItem('therapy_display_name', this.displayName);
            localStorage.setItem('therapy_participant_role', this.participantRole);
            localStorage.setItem('therapy_user_id', this.callerUserId);

            try {
                this.localUid = this.buildUid();
                await this.client.join(this.appId, room, this.token, this.localUid);

                this.joined = true;
                this.micEnabled = false;
                this.camEnabled = false;

                const publishTracks = [];
                const deniedDevices = [];
                const isViewerOnly = this.actionMode === 'watch-live';

                if (isViewerOnly) {
                    this.notice = 'Viewer mode enabled. You are watching live stream only.';
                }

                if (!isViewerOnly) {
                    try {
                        this.localAudioTrack = await AgoraRTC.createMicrophoneAudioTrack();
                        this.micEnabled = true;
                        publishTracks.push(this.localAudioTrack);
                    } catch (micErr) {
                        deniedDevices.push('microphone');
                        this.localAudioTrack = null;
                        console.warn('Microphone unavailable:', micErr);
                    }

                    try {
                        this.localVideoTrack = await AgoraRTC.createCameraVideoTrack();
                        this.camEnabled = true;
                        publishTracks.push(this.localVideoTrack);
                    } catch (camErr) {
                        deniedDevices.push('camera');
                        this.localVideoTrack = null;
                        console.warn('Camera unavailable:', camErr);
                    }
                }

                if (publishTracks.length > 0) {
                    await this.client.publish(publishTracks);
                }

                if (deniedDevices.length > 0) {
                    this.notice = `Joined session, but ${deniedDevices.join(' and ')} permission was denied.`;
                }

                if (1 + this.client.remoteUsers.length > this.selectedActionMode.maxParticipants) {
                    this.notice = `${this.selectedActionMode.label} is best for up to ${this.selectedActionMode.maxParticipants} participants.`;
                }

                // Catch remote users already in room and retry shortly for late-published tracks.
                await this.syncAndSubscribeAll();
                setTimeout(() => {
                    this.syncAndSubscribeAll();
                }, 1200);

                this.$nextTick(() => {
                    if (this.localVideoTrack) {
                        this.localVideoTrack.play('local-player', { fit: 'cover' });
                    }
                });
            } catch (err) {
                this.error = `Failed to join video call: ${err.message}`;
                console.error(err);
            } finally {
                this.loading = false;
            }
        },
        handleUserJoined(user) {
            this.upsertRemoteUser(user);
            setTimeout(() => {
                this.syncAndSubscribeAll();
            }, 500);
        },
        async handleUserPublished(user, mediaType) {
            this.upsertRemoteUser(user);
            const subscribed = await this.subscribeWithRetry(user, mediaType);
            if (!subscribed) {
                this.notice = `A participant joined, but media subscription failed (${this.lastSubscriptionError || 'unknown'}). Retrying...`;
                setTimeout(() => {
                    this.recoverRemoteMedia(user.uid, mediaType);
                }, 1200);
                return;
            }

            await this.playRemoteTracks(user, mediaType);

            if (1 + this.remoteUsers.length > this.selectedActionMode.maxParticipants) {
                this.notice = `${this.selectedActionMode.label} participant limit exceeded for this session format.`;
            }
        },
        handleUserUnpublished(user) {
            const index = this.remoteUsers.findIndex(remote => String(remote.uid) === String(user.uid));
            if (index >= 0) {
                this.remoteUsers.splice(index, 1, user);
            }
        },
        handleUserLeft(user) {
            this.remoteUsers = this.remoteUsers.filter(remote => String(remote.uid) !== String(user.uid));
        },
        async toggleMic() {
            if (!this.localAudioTrack) {
                return;
            }
            await this.localAudioTrack.setEnabled(!this.micEnabled);
            this.micEnabled = !this.micEnabled;
        },
        async toggleCam() {
            if (!this.localVideoTrack) {
                return;
            }
            await this.localVideoTrack.setEnabled(!this.camEnabled);
            this.camEnabled = !this.camEnabled;
        },
        async leaveCall() {
            try {
                this.notice = '';
                this.error = '';
                this.remoteUsers = [];

                if (this.localAudioTrack) {
                    this.localAudioTrack.close();
                    this.localAudioTrack = null;
                }
                if (this.localVideoTrack) {
                    this.localVideoTrack.close();
                    this.localVideoTrack = null;
                }

                if (this.client && this.joined) {
                    await this.client.leave();
                }
            } catch (err) {
                console.error('Failed to leave call:', err);
            } finally {
                this.joined = false;
                this.localUid = null;
                this.activeChannelName = '';
                this.micEnabled = true;
                this.camEnabled = true;
            }
        },
    },
};
</script>

<style scoped>
.call-shell {
    --ink: #0f172a;
    --muted: #48607a;
    --surface: #f9fcff;
    --card: rgba(255, 255, 255, 0.88);
    --line: rgba(13, 50, 87, 0.15);
    --brand: #007f8c;
    --brand-soft: #ddf8fb;
    --accent: #ff8a3d;
    position: relative;
    min-height: calc(100vh - 120px);
    width: min(1200px, calc(100vw - 300px));
    margin: 0 auto;
    padding: 22px;
    overflow: hidden;
    color: var(--ink);
    font-family: 'Nunito Sans', 'Trebuchet MS', sans-serif;
    background: radial-gradient(circle at top right, #eaf9ff 0%, #f5fdff 46%, #fff8ef 100%);
    border-radius: 20px;
}

.bg-orb {
    position: absolute;
    border-radius: 999px;
    filter: blur(10px);
    opacity: 0.35;
    pointer-events: none;
}

.orb-a {
    width: 260px;
    height: 260px;
    background: #90e0ef;
    top: -80px;
    right: -90px;
}

.orb-b {
    width: 210px;
    height: 210px;
    background: #ffcd99;
    bottom: -70px;
    left: -70px;
}

.call-header {
    position: relative;
    z-index: 2;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 16px;
}

.eyebrow {
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-size: 0.75rem;
    color: #0f6b74;
    font-weight: 800;
}

.call-header h2 {
    margin: 4px 0;
    font-size: clamp(1.35rem, 2.2vw, 2rem);
    line-height: 1.1;
}

.subtitle {
    margin: 0;
    color: var(--muted);
    max-width: 720px;
}

.mode-pill {
    background: linear-gradient(130deg, #0f6b74, #007f8c);
    color: #fff;
    border-radius: 14px;
    padding: 10px 14px;
    font-weight: 700;
    display: grid;
    gap: 4px;
    min-width: 170px;
    box-shadow: 0 14px 22px rgba(15, 107, 116, 0.18);
}

.mode-pill span {
    color: #cffaff;
    font-size: 0.8rem;
    font-weight: 600;
}

.status {
    position: relative;
    z-index: 2;
    border-radius: 12px;
    padding: 10px 14px;
    margin-bottom: 12px;
    font-weight: 700;
}

.status-info {
    background: #d8f3ff;
    color: #0b5d8b;
}

.status-warn {
    background: #fff3d8;
    color: #8e5a1a;
}

.status-error {
    background: #ffe3e3;
    color: #952121;
}

.prejoin-card,
.toolbar,
.social-hint {
    position: relative;
    z-index: 2;
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: 16px;
    box-shadow: 0 10px 24px rgba(34, 78, 112, 0.08);
}

.prejoin-card {
    padding: 18px;
    display: grid;
    gap: 14px;
}

.prejoin-card h3 {
    margin: 0;
    font-size: 1.2rem;
}

.grid-2 {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
}

.field {
    display: grid;
    gap: 6px;
    color: var(--muted);
    font-weight: 700;
    font-size: 0.92rem;
}

.input {
    border-radius: 12px;
    border: 1px solid #bfd7e7;
    background: #ffffff;
    color: var(--ink);
    padding: 10px 12px;
    font-size: 0.95rem;
}

.input:focus {
    outline: 2px solid #9ee8ef;
    border-color: #4fa8b0;
}

.mode-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
}

.access-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
}

.mode-card {
    text-align: left;
    border-radius: 14px;
    border: 1px solid #bfd7e7;
    background: #fff;
    padding: 12px;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.mode-card strong {
    display: block;
    font-size: 0.98rem;
    margin-bottom: 4px;
}

.mode-card p {
    margin: 0;
    color: #4f657d;
    font-size: 0.86rem;
}

.mode-card:hover {
    transform: translateY(-1px);
    border-color: #4fa8b0;
}

.mode-card.active {
    border-color: #0f6b74;
    background: #e8fbff;
    box-shadow: 0 8px 20px rgba(15, 107, 116, 0.13);
}

.goal-note {
    margin: 0;
    padding: 10px 12px;
    border-radius: 12px;
    background: var(--brand-soft);
    color: #0f6b74;
    font-weight: 700;
}

.access-note {
    background: #ebf4ff;
    color: #1b4668;
}

.session-wrap {
    position: relative;
    z-index: 2;
    display: grid;
    gap: 12px;
}

.toolbar {
    padding: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.session-facts {
    display: grid;
    gap: 4px;
    font-size: 0.9rem;
    color: #34506b;
}

.session-facts p {
    margin: 0;
}

.actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.btn {
    border: 1px solid #a8c8de;
    background: #fff;
    color: #173451;
    border-radius: 11px;
    padding: 10px 12px;
    font-size: 0.9rem;
    font-weight: 800;
    cursor: pointer;
}

.btn:hover:not(:disabled) {
    background: #ecf8ff;
}

.btn:disabled {
    opacity: 0.45;
    cursor: not-allowed;
}

.btn-primary {
    width: fit-content;
    background: linear-gradient(120deg, #007f8c, #1a97a3);
    color: #fff;
    border-color: #007f8c;
}

.btn-primary:hover:not(:disabled) {
    background: linear-gradient(120deg, #0b6f7a, #12848f);
}

.btn-danger {
    background: #ffe3dc;
    border-color: #ffb9a8;
    color: #8f2b1f;
}

.social-hint {
    padding: 10px 12px;
    color: #234f56;
}

.video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 12px;
}

.participant {
    position: relative;
    aspect-ratio: 16 / 10;
    border-radius: 14px;
    background: #0f1720;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.participant.local {
    border-color: #55d5e2;
}

.participant-badge,
.participant-name {
    position: absolute;
    left: 10px;
    z-index: 3;
    border-radius: 999px;
    font-size: 0.74rem;
    letter-spacing: 0.02em;
}

.participant-badge {
    top: 10px;
    background: rgba(0, 127, 140, 0.84);
    color: #dbfbff;
    padding: 4px 10px;
    font-weight: 700;
}

.participant-name {
    top: 40px;
    background: rgba(10, 20, 34, 0.74);
    color: #fff;
    padding: 4px 10px;
    font-weight: 800;
}

.media-status {
    position: absolute;
    inset: 0;
    display: grid;
    place-items: center;
    z-index: 2;
    color: #fff;
    background: linear-gradient(0deg, rgba(11, 17, 28, 0.75), rgba(11, 17, 28, 0.45));
}

.media-status span {
    background: rgba(0, 0, 0, 0.58);
    padding: 7px 14px;
    border-radius: 999px;
}

.video-player {
    width: 100%;
    height: 100%;
}

@media (max-width: 980px) {
    .call-shell {
        width: 100%;
        min-height: auto;
        padding: 14px 12px;
        border-radius: 14px;
    }

    .call-header {
        flex-direction: column;
    }

    .mode-pill {
        width: fit-content;
    }

    .grid-2,
    .mode-grid {
        grid-template-columns: 1fr;
    }

    .toolbar {
        flex-direction: column;
        align-items: flex-start;
    }

    .actions {
        width: 100%;
    }

    .actions .btn {
        flex: 1 1 140px;
    }
}

@media (max-width: 600px) {
    .call-shell {
        padding: 12px 8px;
    }

    .subtitle {
        font-size: 0.95rem;
    }

    .mode-pill {
        min-width: 0;
        width: 100%;
    }

    .prejoin-card,
    .toolbar,
    .social-hint {
        border-radius: 14px;
    }

    .prejoin-card {
        padding: 12px;
        gap: 10px;
    }

    .mode-card {
        padding: 10px;
    }

    .actions {
        width: 100%;
    }

    .actions .btn {
        min-width: 0;
        width: 100%;
    }

    .video-grid {
        grid-template-columns: 1fr;
        gap: 10px;
    }

    .participant {
        aspect-ratio: 4 / 3;
        border-radius: 12px;
    }
}
</style>