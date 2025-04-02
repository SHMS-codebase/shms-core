document.addEventListener("DOMContentLoaded", function() {
	populateSpecializations();
});

function populateSpecializations() {
	const specializations = [
		"General Physician", "Cardiology", "Dermatology", "Emergency Medicine", "Endocrinology",
		"Gastroenterology", "Neurology", "Oncology", "Orthopedics", "Pediatrics", "Psychiatry", 
		"Radiology", "ENT", "Nephrology", "Urology", "Rheumatology", "Pulmonology", "Hematology",
		"Ophthalmology", "Plastic Surgery", "Geriatrics", "Obstetrics and Gynecology", "Anesthesiology"
	]; // Add or fetch dynamically from API if needed

	const specializationDropdown = document.getElementById("specialization");
	if (!specializationDropdown) return; // Ensure element exists

	// Clear existing options
	specializationDropdown.innerHTML = '<option value="">Select Specialization</option>';

	// Populate new options
	specializations.forEach(spec => {
		const option = document.createElement("option");
		option.value = spec;
		option.textContent = spec;
		specializationDropdown.appendChild(option);
	});
}



function loadFields(role) {

	// Clear all fields before showing new ones
	clearAllFields();

	// Clear previous error messages
	document.querySelectorAll('.error-message').forEach(el => el.remove());

	// Show/hide fields based on the selected role
	document.getElementById('adminFields').style.display = role === 'Admin' ? 'block' : 'none';
	document.getElementById('doctorFields').style.display = role === 'Doctor' ? 'block' : 'none';
	document.getElementById('patientFields').style.display = role === 'Patient' ? 'block' : 'none';

	// Clear and repopulate the specialization dropdown if the role is Doctor


}

function clearAllFields() {
	const inputFields = document.querySelectorAll('input, select,textarea');
	inputFields.forEach(field => {

		// To skip clearing the role dropdown 
		if (field.id === "role") return;

		if (field.type === 'text' || field.type === 'email' || field.type === 'number'
			|| field.type === 'date' || field.type === 'tel' || field.tagName === 'TEXTAREA') {
			field.value = '';
		} else if (field.tagName === 'SELECT') {
			field.selectedIndex = 0;
		}
	});
}

function ensureUserRoleDropdownVisible() {
	const roleElement = document.getElementById('role');
	if (roleElement.style.display === 'none') {
		roleElement.style.display = 'block';
	}
}

function validateForm() {
	const role = document.getElementById('role').value;
	console.log(role);
	let valid = true;

	// Implemented new error caching mechanism
	const errorCache = new Map();

	// Clear previous errors
	document.querySelectorAll('.error-message').forEach(el => el.remove());

	// Clear the error cache to ensure caching mechanism does not clear old errors every time validation occurs
	errorCache.clear();

	// Common validation function
	function validateField(fieldId, rules) {
		const field = document.getElementById(fieldId);
		if (!field) return;
		for (let rule of rules) {
			if (!rule.validate(field.value)) {
				valid = false;
				showError(field, rule.message);
			}
		}
	}



	function showError(field, message) {
		// Check if error message template exists in cache
		if (!errorCache.has(message)) {
			const error = document.createElement('div');
			error.className = 'error-message';
			error.style.color = 'red';
			// Store the error template in cache
			errorCache.set(message, error);
		}

		// Clone the cached error element
		const errorElement = errorCache.get(message).cloneNode(true);
		errorElement.innerText = message;

		// Insert the error message after the input field
		field.parentNode.insertBefore(errorElement, field.nextSibling);
	}

	// Admin Fields Validation
	if (role === 'Admin') {
		validateField('adminName', [
			{ validate: (value) => value.trim() !== '', message: 'Admin Name is mandatory.' },
			{ validate: (value) => value.length >= 3 && value.length <= 50, message: 'Length 3-50 characters' },
			{ validate: (value) => /^[a-zA-Z0-9 ]+$/.test(value), message: 'No special charcters allowed.' }
		]);
		validateField('adminEmailID', [
			{ validate: (value) => value.trim() !== '', message: 'Email ID is mandatory.' },
			{ validate: (value) => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value), message: 'Provide a valid email address.' }
		]);
	}

	// Doctor Fields Validation
	if (role === 'Doctor') {
		validateField('doctorName', [
			{ validate: (value) => value.trim() !== '', message: 'Doctor Name is mandatory.' },
			{ validate: (value) => value.length >= 3 && value.length <= 50, message: 'Length 3-50 characters.' },
			{ validate: (value) => /^[a-zA-Z0-9 ]+$/.test(value), message: 'No special charcters allowed.' }
		]);
		validateField('doctorContactNumber', [
			{ validate: (value) => value.trim() !== '', message: 'Contact Number is mandatory.' },
			{ validate: (value) => /^[0-9]{10}$/.test(value), message: 'Must be a valid 10-digit number.' }
		]);
		validateField('doctorEmailID', [
			{ validate: (value) => value.trim() !== '', message: 'Email ID is required.' },
			{ validate: (value) => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value), message: 'Provide a valid email address.' }
		]);
		validateField('doctorAddress', [
			{ validate: (value) => value.trim() !== '', message: 'Address is mandatory.' },
			{ validate: (value) => value.length <= 255, message: 'Must be 255 characters or less.' }
		]);
		validateField('qualification', [
			{ validate: (value) => value.trim() !== '', message: 'Qualification is mandatory.' },
			{ validate: (value) => value.length <= 100, message: 'Must be 100 characters or less.' }
		]);
		validateField('specialization', [
			{ validate: (value) => value.trim() !== '', message: 'Specialization is mandatory.' }
		]);
		validateField('experience', [
			{ validate: (value) => value.trim() !== '', message: 'Experience is mandatory.' },
			{ validate: (value) => /^[0-9]+$/.test(value), message: 'Must be a numeric value.' },
			{ validate: (value) => value >= 0 && value <= 40, message: 'Must be between 0 and 40 years.' }
		]);
	}

	// Patient Fields Validation
	if (role === 'Patient') {
		validateField('patientName', [
			{ validate: (value) => value.trim() !== '', message: 'Patient Name is mandatory.' },
			{ validate: (value) => value.length >= 3 && value.length <= 50, message: 'Length 3-50 characters.' },
			{ validate: (value) => /^[a-zA-Z0-9 ]+$/.test(value), message: 'No special charcters allowed.' }
		]);
		validateField('dob', [
			{ validate: (value) => value.trim() !== '', message: 'Date of Birth is mandatory.' },
			{ validate: (value) => /^\d{4}-\d{2}-\d{2}$/.test(value), message: 'Must be in the format YYYY-MM-DD.' },
			{ validate: (value) => new Date(value) <= new Date(), message: 'Value cannot be in the future.' }
		]);
		validateField('gender', [
			{ validate: (value) => value.trim() !== '', message: 'Gender is mandatory.' }
		]);
		validateField('patientContactNumber', [
			{ validate: (value) => value.trim() !== '', message: 'Contact Number is mandatory.' },
			{ validate: (value) => /^[0-9]{10}$/.test(value), message: 'Must be a valid 10-digit number.' }
		]);
		validateField('patientEmailID', [
			{ validate: (value) => value.trim() !== '', message: 'Email ID is mandatory.' },
			{ validate: (value) => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value), message: 'Provide a valid email address.' }
		]);
		validateField('patientAddress', [
			{ validate: (value) => value.trim() !== '', message: 'Address is mandatory.' },
			{ validate: (value) => value.length <= 255, message: 'Must be 255 characters or less.' }
		]);
		// // New Medical History Validation
		// validateField('medicalHistory', [
		// 	{ validate: (value) => value.trim() !== '', message: 'Medical History is mandatory.' },
		// 	{ validate: (value) => value.length >= 10, message: 'Must be at least 10 characters long.' },
		// 	{ validate: (value) => value.length <= 1000, message: 'Must be 1000 characters or less.' }
		// ]);

	}
	return valid;
}