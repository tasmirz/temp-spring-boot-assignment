// Dashboard JS

// Check authentication immediately
function checkAuth() {
    const authToken = localStorage.getItem('token');
    if (!authToken) {
        console.log('No token found, redirecting to login...');
        window.location.href = '/';
        return false;
    }
    return true;
}

// Check auth before anything else
if (!checkAuth()) {
    throw new Error('Not authenticated');
}

let token = localStorage.getItem('token');
let username = localStorage.getItem('username');
let userRole = localStorage.getItem('role') || ''; // Store role globally
let currentStudentId = null; // Store student ID globally

// Show dashboard container after auth check passes
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('dashboardContainer').style.display = 'block';
});

// Check authentication on page load
window.addEventListener('load', () => {
    if (!token) {
        window.location.href = '/';
        return;
    }
    
    // Decode token to get role (JWT format: header.payload.signature)
    decodeToken();
    loadDashboard();
});

function decodeToken() {
    try {
        const parts = token.split('.');
        if (parts.length === 3) {
            const payload = JSON.parse(atob(parts[1]));
            console.log('Token payload:', payload);
            
            const role = payload.role || localStorage.getItem('role') || '';
            userRole = role; // Update global variable
            console.log('Role from token:', role);
            
            const userRoleElement = document.getElementById('userRole');
            if (userRoleElement) {
                // Remove 'ROLE_' prefix if present
                const displayRole = role.replace('ROLE_', '') || 'User';
                userRoleElement.textContent = displayRole;
                console.log('Role set to:', displayRole);
            }
            
            // Show/hide teacher section based on role
            const isTeacher = role && role.includes('TEACHER');
            console.log('Is teacher:', isTeacher);
            
            const teacherSection = document.getElementById('teacherSection');
            const studentActionsHeader = document.getElementById('studentActionsHeader');
            
            if (isTeacher) {
                if (teacherSection) teacherSection.style.display = 'block';
                console.log('Teacher section displayed');
            } else {
                if (teacherSection) teacherSection.style.display = 'none';
                console.log('Teacher section hidden');
            }
        }
    } catch (e) {
        console.error('Error decoding token:', e);
        window.location.replace('/login');
    }
}

function logout() {
    console.log('Logout button clicked');
    try {
        localStorage.clear();
        sessionStorage.clear();
        console.log('Storage cleared successfully');
        
        // Use replace to prevent browser history issues
        window.location.replace('/');
    } catch (e) {
        console.error('Error during logout:', e);
        window.location.replace('/');
    }
}

async function loadDashboard() {
    await loadCourses();
    const isTeacher = userRole && userRole.includes('TEACHER');
    if (isTeacher) {
        await loadStudents();
        await loadStudentsForEnrollment();
    } else {
        await loadMyEnrollments();
    }
}

// COURSES SECTION
async function loadCourses() {
    console.log('loadCourses called, token:', token);
    try {
        const headers = {
            'Authorization': `Bearer ${token}`
        };
        console.log('Request headers:', headers);
        
        const response = await fetch(`${API_BASE_URL}/api/courses`, {
            method: 'GET',
            headers: headers
        });
        
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const courses = await response.json();
            console.log('Courses loaded:', courses);
            
            // For students, pass their enrolled course IDs
            const isTeacher = userRole && userRole.includes('TEACHER');
            if (isTeacher) {
                displayCourses(courses);
            } else {
                displayCourses(courses, currentStudentEnrollments);
            }
        } else if (response.status === 401) {
            console.log('Unauthorized - logging out');
            logout();
        } else {
            console.log('Error loading courses:', response.status);
        }
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

function displayCourses(courses, enrolledCourseIds = []) {
    const tbody = document.getElementById('coursesBody');
    tbody.innerHTML = '';
    
    // Check if user is a teacher based on global userRole variable
    const isTeacher = userRole && userRole.includes('TEACHER');
    console.log('Displaying courses - is teacher:', isTeacher);
    
    courses.forEach(c => {
        const row = tbody.insertRow();
        let actions = '';
        
        if (isTeacher) {
            actions = `
                <button onclick="editCourse(${c.id})">Edit</button>
                <button onclick="deleteCourse(${c.id})">Delete</button>
            `;
        } else {
            // Student enrollment/unenrollment buttons
            const isEnrolled = enrolledCourseIds.includes(c.id);
            if (isEnrolled) {
                actions = `<button onclick="unenrollCourse(${c.id})">Unenroll</button>`;
            } else {
                actions = `<button onclick="enrollCourse(${c.id})">Enroll</button>`;
            }
        }
        
        row.innerHTML = `
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.code}</td>
            <td>${actions}</td>
        `;
    });
}


async function saveCourse(event) {
    event.preventDefault();
    
    const courseId = document.getElementById('courseId').value;
    const course = {
        name: document.getElementById('courseName').value,
        code: document.getElementById('courseCode').value
    };
    
    try {
        const url = courseId ? `${API_BASE_URL}/api/courses/${courseId}` : `${API_BASE_URL}/api/courses`;
        const method = courseId ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(course)
        });
        
        if (response.ok) {
            alert('Course saved successfully!');
            document.getElementById('courseForm').reset();
            document.getElementById('courseId').value = '';
            document.getElementById('courseSubmitBtn').textContent = 'Add Course';
            loadCourses();
        } else if (response.status === 401) {
            logout();
        } else {
            alert('Error saving course');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error saving course');
    }
}

async function editCourse(courseId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/courses/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.ok) {
            const course = await response.json();
            document.getElementById('courseId').value = course.id;
            document.getElementById('courseName').value = course.name;
            document.getElementById('courseCode').value = course.code;
            document.getElementById('courseSubmitBtn').textContent = 'Update Course';
            document.getElementById('addCourseSection').scrollIntoView();
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error fetching course');
    }
}

async function deleteCourse(courseId) {
    if (!confirm('Are you sure you want to delete this course?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/api/courses/${courseId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            alert('Course deleted successfully!');
            loadCourses();
        } else if (response.status === 401) {
            logout();
        } else {
            alert('Error deleting course');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error deleting course');
    }
}

// STUDENTS SECTION (Teachers only)
async function loadStudents() {
    const isTeacher = userRole && userRole.includes('TEACHER');
    if (!isTeacher) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/api/students`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const students = await response.json();
            displayStudents(students);
        } else if (response.status === 401) {
            logout();
        }
    } catch (error) {
        console.error('Error loading students:', error);
    }
}

function displayStudents(students) {
    const tbody = document.getElementById('studentsBody');
    tbody.innerHTML = '';
    
    students.forEach(student => {
        const row = tbody.insertRow();
        // Display course names (for M:M relationship)
        const isTeacherView = userRole && userRole.includes('TEACHER');
        let courseNamesHtml = 'N/A';
        if (student.courses && student.courses.length > 0) {
            courseNamesHtml = student.courses.map(c => {
                if (isTeacherView) {
                    return `${c.name} <button onclick="unenrollStudent(${student.id}, ${c.id})">Unenroll</button>`;
                }
                return c.name;
            }).join(', ');
        }

        row.innerHTML = `
            <td>${student.id}</td>
            <td>${student.name}</td>
            <td>${student.email}</td>
            <td>${courseNamesHtml}</td>
            <td>${isTeacherView ? '<button onclick="selectStudentForEnrollment(' + student.id + ')">Manage</button>' : ''}</td>
        `;
    });
}

// Teacher: unenroll a specific student from a specific course
async function unenrollStudent(studentId, courseId) {
    if (!confirm('Unenroll this student from the course?')) return;
    try {
        const response = await fetch(`${API_BASE_URL}/api/students/${studentId}/unenroll/${courseId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (response.ok) {
            alert('Student unenrolled successfully');
            // refresh students and courses
            await loadStudentsForEnrollment();
            await loadCourses();
        } else if (response.status === 401) {
            logout();
        } else {
            const text = await response.text();
            console.error('Unenroll error:', text);
            alert('Error unenrolling student');
        }
    } catch (err) {
        console.error('Error:', err);
        alert('Error unenrolling student');
    }
}

function selectStudentForEnrollment(studentId) {
    const select = document.getElementById('enrollmentStudent');
    select.value = studentId;
    loadStudentCourses();
}


// STUDENT ENROLLMENT SECTION
let currentStudentEnrollments = [];

// Load student's enrolled courses
async function loadMyEnrollments() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/students/me`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (response.ok) {
            const currentStudent = await response.json();
            currentStudentId = currentStudent.id;
            console.log('Current student ID:', currentStudentId);
            if (currentStudent.courses) {
                currentStudentEnrollments = currentStudent.courses.map(c => c.id);
                console.log('My enrollments:', currentStudentEnrollments);
            }
            // Reload courses to show correct enrollment status
            await loadCourses();
        } else if (response.status === 401) {
            logout();
        }
    } catch (error) {
        console.error('Error loading enrollments:', error);
    }
}

// Enroll student in a course
async function enrollCourse(courseId) {
    if (!currentStudentId) {
        alert('Student ID not found. Please reload the page.');
        return;
    }
    
    try {
        // If current user is a student, use the /me endpoint so they don't need teacher privileges
        const enrollUrl = (userRole && userRole.includes('STUDENT'))
            ? `${API_BASE_URL}/api/students/me/enroll/${courseId}`
            : `${API_BASE_URL}/api/students/${currentStudentId}/enroll/${courseId}`;

        const response = await fetch(enrollUrl, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            alert('Enrolled successfully!');
            await loadMyEnrollments();
        } else if (response.status === 401) {
            logout();
        } else {
            const text = await response.text();
            console.error('Enrollment error:', text);
            alert('Error enrolling in course');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error enrolling in course');
    }
}

// Unenroll student from a course
async function unenrollCourse(courseId) {
    if (!confirm('Are you sure you want to unenroll from this course?')) {
        return;
    }
    
    if (!currentStudentId) {
        alert('Student ID not found. Please reload the page.');
        return;
    }
    
    try {
        // Use /me for student self-service to avoid needing teacher role
        const unenrollUrl = (userRole && userRole.includes('STUDENT'))
            ? `${API_BASE_URL}/api/students/me/unenroll/${courseId}`
            : `${API_BASE_URL}/api/students/${currentStudentId}/unenroll/${courseId}`;

        const response = await fetch(unenrollUrl, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            alert('Unenrolled successfully!');
            await loadMyEnrollments();
        } else if (response.status === 401) {
            logout();
        } else {
            alert('Error unenrolling from course');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error unenrolling from course');
    }
}

// Load students for enrollment management (teachers only)
async function loadStudentsForEnrollment() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/students`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const students = await response.json();
            populateStudentSelect(students);
        } else if (response.status === 401) {
            logout();
        }
    } catch (error) {
        console.error('Error loading students:', error);
    }
}

function populateStudentSelect(students) {
    const select = document.getElementById('enrollmentStudent');
    select.innerHTML = '<option value="">Select a Student</option>';
    
    students.forEach(s => {
        const option = document.createElement('option');
        option.value = s.id;
        option.textContent = `${s.name} (${s.rollNumber})`;
        select.appendChild(option);
    });
}

function loadStudentCourses() {
    const studentSelect = document.getElementById('enrollmentStudent');
    const studentId = studentSelect.value;
    
    if (!studentId) {
        document.getElementById('enrollmentCourse').innerHTML = '<option value="">Select Course</option>';
        return;
    }
    
    // Find the student in the displayed students list
    fetch(`${API_BASE_URL}/api/students/${studentId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(r => r.json())
    .then(student => {
        const enrolledIds = student.courses.map(c => c.id);
        const courseSelect = document.getElementById('enrollmentCourse');
        
        // Get all courses to show uneinrolled ones
        fetch(`${API_BASE_URL}/api/courses`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(r => r.json())
        .then(courses => {
            courseSelect.innerHTML = '<option value="">Select Course</option>';
            courses.forEach(c => {
                if (!enrolledIds.includes(c.id)) {
                    const option = document.createElement('option');
                    option.value = c.id;
                    option.textContent = `${c.name} (${c.code})`;
                    courseSelect.appendChild(option);
                }
            });
        });
    });
}

// Save enrollment (add course to student)
async function saveEnrollment(event) {
    event.preventDefault();
    
    const studentId = document.getElementById('enrollmentStudent').value;
    const courseId = document.getElementById('enrollmentCourse').value;
    
    if (!studentId || !courseId) {
        alert('Please select both a student and a course');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/api/students/${studentId}/enroll/${courseId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            alert('Course added to student successfully!');
            document.getElementById('enrollmentForm').reset();
            await loadStudents();
        } else if (response.status === 401) {
            logout();
        } else {
            alert('Error adding course to student');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error adding course to student');
    }
}

