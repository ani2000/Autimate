# Contributing to Autimate

First off, thank you for considering contributing to Autimate! This is a social-impact project targeting underserved communities in Bangladesh, and every contribution helps improve early ASD detection and therapy delivery.

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of background, identity, or experience level.

### Standards

- Use welcoming and inclusive language
- Be respectful of differing viewpoints
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other contributors

---

## How Can I Contribute?

### 1. Reporting Bugs

**Before submitting a bug report:**
- Check existing [GitHub Issues](https://github.com/ani2000/autimate/issues)
- Verify the bug exists in the latest version
- Collect relevant information (OS, browser, Java/Node versions)

**Submitting a bug report:**

Use the bug report template:

```markdown
**Description:**
Clear description of the bug

**Steps to Reproduce:**
1. Go to '...'
2. Click on '...'
3. See error

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happens

**Environment:**
- OS: [e.g., Windows 11]
- Browser: [e.g., Chrome 120]
- Java Version: [e.g., OpenJDK 21]
- Node Version: [e.g., v18.17.0]

**Screenshots:**
If applicable

**Additional Context:**
Any other relevant information
```

### 2. Suggesting Enhancements

**Enhancement categories:**
- New therapy modules
- ML model improvements
- UI/UX enhancements
- Performance optimizations
- Accessibility features
- Localization (Bengali language support)

**Proposal template:**

```markdown
**Feature Name:**
[Brief name]

**Problem Statement:**
What problem does this solve?

**Proposed Solution:**
How should it work?

**Alternatives Considered:**
Other approaches

**Impact:**
Who benefits? How?

**Implementation Complexity:**
[Low/Medium/High]
```

### 3. Contributing Code

#### Getting Started

1. **Fork the repository**

```bash
git clone https://github.com/ani2000/autimate.git
cd autimate
```

2. **Create a feature branch**

```bash
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` — New features (e.g., `feature/emotion-recognition-game`)
- `fix/` — Bug fixes (e.g., `fix/video-upload-timeout`)
- `refactor/` — Code refactoring (e.g., `refactor/auth-service`)
- `docs/` — Documentation updates (e.g., `docs/api-endpoints`)
- `test/` — Test additions (e.g., `test/questionnaire-validation`)

3. **Set up development environment**

Follow [SETUP.md](SETUP.md) for complete instructions.

4. **Make your changes**

**Code style guidelines:**

**Java/Spring Boot:**
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable names: `userRepository` not `ur`
- Add JavaDoc for public methods
- Maximum line length: 120 characters

```java
/**
 * Analyzes video tensor for ASD behavioral markers.
 *
 * @param videoTensor Input tensor in (C, T, H, W) format
 * @param childId Unique identifier for the child
 * @return ASDExEntity containing probability score
 * @throws VideoProcessingException if inference fails
 */
public ASDExEntity analyzeVideo(Tensor videoTensor, String childId) {
    // Implementation
}
```

**Vue.js/JavaScript:**
- Follow [Vue.js Style Guide](https://vuejs.org/style-guide/)
- Use Composition API with `<script setup>`
- Component names: PascalCase (e.g., `DrawingGame.vue`)
- Use TypeScript when possible

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface TherapySession {
  id: string
  duration: number
  score: number
}

const sessions = ref<TherapySession[]>([])

const fetchSessions = async () => {
  // Implementation
}

onMounted(() => {
  fetchSessions()
})
</script>
```

**Python/ML:**
- Follow [PEP 8](https://peps.python.org/pep-0008/)
- Type hints for all functions
- Docstrings for modules/classes/functions

```python
from typing import Tuple
import numpy as np

def preprocess_video(
    frames: np.ndarray,
    target_size: Tuple[int, int] = (224, 224)
) -> np.ndarray:
    """
    Preprocess video frames for ONNX model inference.
    
    Args:
        frames: Raw video frames (T, H, W, C)
        target_size: Output spatial dimensions
        
    Returns:
        Preprocessed tensor in (C, T, H, W) format
        
    Raises:
        ValueError: If frames shape is invalid
    """
    # Implementation
```

5. **Write tests**

**Backend (JUnit 5):**

```java
@SpringBootTest
class ASDExServiceTest {
    
    @Autowired
    private ASDExService asdExService;
    
    @Test
    void testVideoAnalysis_ValidInput_ReturnsScore() {
        // Given
        String childId = "test-child-123";
        VideoRequest request = new VideoRequest(/* ... */);
        
        // When
        ASDExEntity result = asdExService.analyzeVideo(request, childId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getScore() >= 0.0 && result.getScore() <= 1.0);
    }
}
```

**Frontend (Vitest):**

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DrawingGame from '@/components/therapy/DrawingGame.vue'

describe('DrawingGame', () => {
  it('renders canvas element', () => {
    const wrapper = mount(DrawingGame)
    expect(wrapper.find('canvas').exists()).toBe(true)
  })
  
  it('emits feedback-received event', async () => {
    const wrapper = mount(DrawingGame)
    await wrapper.vm.submitDrawing()
    expect(wrapper.emitted('feedback-received')).toBeTruthy()
  })
})
```

**Python (pytest):**

```python
import pytest
import numpy as np
from video_onnx_proc import preprocess_video

def test_preprocess_video_shape():
    """Test output tensor has correct shape."""
    frames = np.random.rand(16, 480, 640, 3)  # (T, H, W, C)
    result = preprocess_video(frames)
    
    assert result.shape == (3, 16, 224, 224)  # (C, T, H, W)

def test_preprocess_video_normalization():
    """Test pixel values are normalized to [0, 1]."""
    frames = np.random.randint(0, 256, (16, 480, 640, 3), dtype=np.uint8)
    result = preprocess_video(frames)
    
    assert result.min() >= 0.0
    assert result.max() <= 1.0
```

6. **Commit your changes**

**Commit message format:**

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks

**Examples:**

```bash
git commit -m "feat(therapy): Add emotion recognition game module

- Implement facial expression classification
- Integrate with Gemini Vision API
- Add progress tracking dashboard
- Include audio feedback for correct matches

Closes #42"
```

```bash
git commit -m "fix(auth): Resolve JWT token expiration race condition

Token refresh was failing when requests arrived within 1s of expiration.
Now using 30s buffer window before refresh.

Fixes #78"
```

7. **Push to your fork**

```bash
git push origin feature/your-feature-name
```

8. **Open a Pull Request**

**PR template:**

```markdown
## Description
Brief summary of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issue
Closes #[issue_number]

## Testing
Describe testing performed:
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added/updated
- [ ] All tests pass

## Screenshots (if applicable)
[Add screenshots for UI changes]

## Additional Notes
Any other context
```

---

## Development Workflow

### Branch Protection Rules

- `main` branch is protected
- Requires 1 approval before merge
- All CI checks must pass
- Branch must be up to date before merge

### Code Review Process

1. **Automated checks:**
   - Linting (ESLint, Checkstyle)
   - Unit tests (JUnit, Vitest, pytest)
   - Build verification
   - Security scanning

2. **Manual review:**
   - Code quality
   - Architecture alignment
   - Performance considerations
   - Accessibility compliance

3. **Feedback:**
   - Address all review comments
   - Push updates to the same branch
   - Re-request review after changes

---

## Areas Needing Contribution

### High Priority

- **Bengali Language Support** — i18n for frontend
- **Offline PWA Mode** — Service workers for rural areas
- **Model Retraining Pipeline** — Active learning from new data
- **Accessibility Audit** — WCAG 2.1 AA compliance
- **Mobile App** — React Native/Flutter version

### Medium Priority

- **WhatsApp Integration** — Test reminders via bot
- **Video Compression** — Reduce upload sizes
- **Doctor Portal** — Separate interface for specialists
- **Analytics Dashboard** — Admin insights on usage
- **Automated Testing** — E2E tests with Playwright

### Research Contributions

- **Eye-tracking Analysis** — Gaze pattern detection
- **Voice Biomarkers** — Prosody/tone analysis
- **Drawing Complexity Metrics** — Advanced motor skill assessment
- **Multi-language Support** — Expand beyond Bengali

---

## Community

### Communication Channels

- **GitHub Discussions** — Feature proposals, Q&A
- **GitHub Issues** — Bug reports, task tracking
- **Email** — anindyahdec2021@gmail.com

### Recognition

Contributors will be:
- Listed in [CONTRIBUTORS.md](CONTRIBUTORS.md)
- Mentioned in release notes
- Invited to project presentations (if interested)

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License or as specified in [LICENSE](LICENSE)).

---

## Questions?

Don't hesitate to ask! Create a [GitHub Discussion](https://github.com/ani2000/autimate/discussions) or email anindyahdec2021@gmail.com.

**Thank you for helping make early ASD detection accessible to underserved communities!** 🧩💙
