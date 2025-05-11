console.log("this is script file");

function toggleSidebar() {
  if ($(".sidebar").is(":visible")) {
    //true
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    //false
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

// Delete contact function
function deleteContact(id) {
  if (confirm('Are you sure you want to delete this contact?')) {
    window.location.href = '/user/delete/' + id;
  }
}

// Session and form data management
document.addEventListener('DOMContentLoaded', function() {
  // Clear all forms on page load
  clearAllForms();
  
  // Add event listener to clear forms when navigating away from the page
  window.addEventListener('beforeunload', function() {
    clearAllForms();
  });
  
  // Prevent form data from being cached
  preventFormCaching();
});

// Function to clear all forms on the page
function clearAllForms() {
  const forms = document.querySelectorAll('form');
  forms.forEach(function(form) {
    // Add autocomplete="off" to all forms
    form.setAttribute('autocomplete', 'off');
    
    // Add event listener to clear form on submit
    form.addEventListener('submit', function() {
      // Use setTimeout to ensure the form is cleared after submission
      setTimeout(function() {
        form.reset();
      }, 100);
    });
    
    // Get all input fields
    const inputs = form.querySelectorAll('input, textarea, select');
    inputs.forEach(function(input) {
      // Add autocomplete="off" to all inputs
      input.setAttribute('autocomplete', 'off');
      
      // For password fields, use a different autocomplete attribute
      if (input.type === 'password') {
        input.setAttribute('autocomplete', 'new-password');
      }
    });
  });
}

// Function to prevent form caching
function preventFormCaching() {
  // Add cache-busting meta tags
  const head = document.querySelector('head');
  
  // Create and append cache control meta tags
  const metaTags = [
    { name: 'Cache-Control', content: 'no-cache, no-store, must-revalidate' },
    { name: 'Pragma', content: 'no-cache' },
    { name: 'Expires', content: '0' }
  ];
  
  metaTags.forEach(function(meta) {
    const metaTag = document.createElement('meta');
    metaTag.setAttribute('http-equiv', meta.name);
    metaTag.setAttribute('content', meta.content);
    head.appendChild(metaTag);
  });
}
