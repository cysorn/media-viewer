const container = document.querySelector('.container');
const overlay = document.querySelector('.overlay');
const prevArrow = document.getElementById('prevArrow');
const nextArrow = document.getElementById('nextArrow');
const searchBar = document.querySelector('.search-bar');
const tagsManagementContainer = document.querySelector('.tags-management-container');
const buttons = document.querySelectorAll('.hierarchy-button, .tag-button');
const childButtonsContainers = document.querySelectorAll('.child-buttons');
let selectedButton = null;


let fullscreenElement = null;
let scale = 1; // Initial zoom scale
let currentIndex = 0; // Index to keep track of the current media
let wasDragging = false; // New variable to track if the last action was dragging

// Function to update the mediaElements array dynamically
function getMediaElements() {
    return container.querySelectorAll('video, img');
}

// Function to handle media element click
function handleMediaClick(element, index) {
    return () => {
        if (!fullscreenElement) {
            currentIndex = index;
            toggleFullscreen(element);
            if (element.tagName === 'VIDEO') {
                element.play();
            }
        }
    };
}

// Function to toggle fullscreen mode
function toggleFullscreen(element) {
    if (fullscreenElement) {
        fullscreenElement.classList.remove('full-screen');
        fullscreenElement.classList.remove('zoomable'); // Remove zoom class
        fullscreenElement.style.transform = ''; // Reset transform
        fullscreenElement.style.transformOrigin = ''; // Reset transform origin
        fullscreenElement.style.position = ''; // Reset position
        fullscreenElement.style.left = ''; // Reset left
        fullscreenElement.style.top = ''; // Reset top
        fullscreenElement.style.width = ''; // Reset width
        fullscreenElement.style.height = ''; // Reset height
        overlay.style.display = 'none'; // Hide the overlay
		tagsManagementContainer.style.display = 'none';
        fullscreenElement = null;
        scale = 1; // Reset zoom scale
        prevArrow.style.display = 'none'; // Hide arrows
        nextArrow.style.display = 'none';
		searchBar.removeAttribute('readonly'); // Remove readonly when entering fullscreen
		
		buttons.forEach(button => {
			button.classList.remove('selected');
			button.classList.remove('confirmed');
		});
		
		childButtonsContainers.forEach(container => {
			container.style.display = 'none';
		});
		selectedButton = null;
		
    } else {
        element.classList.add('full-screen');
        element.classList.add('zoomable'); // Add zoom class
        element.style.transform = 'scale(1)'; // Ensure scale starts at 1
        element.style.transformOrigin = 'center center'; // Center the zoom
        element.style.position = 'absolute'; // Absolute positioning for dragging and scrolling
        element.style.width = 'auto'; // Allow width to be flexible
        element.style.height = 'auto'; // Allow height to be flexible
        overlay.style.display = 'block'; // Show the overlay
        fullscreenElement = element;
        centerElement(element); // Center the element initially
        prevArrow.style.display = 'block'; // Show arrows
        nextArrow.style.display = 'block';
		searchBar.setAttribute('readonly', true); // Make the search bar non-editable
    }
}

// Function to center the media element
function centerElement(element) {
    const rect = element.getBoundingClientRect();
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;
    const centerX = (viewportWidth - rect.width) / 2;
    const centerY = (viewportHeight - rect.height) / 2;

    element.style.left = `${centerX}px`;
    element.style.top = `${centerY}px`;
}

// Add tabindex="-1" to video elements to prevent focus
function setTabIndexForMediaElements() {
    const mediaElements = getMediaElements();
    mediaElements.forEach(element => {
        if (element.tagName === 'VIDEO') {
            element.tabIndex = -1;
        }
    });
}

// Add event listeners to each media element
function initializeMediaListeners() {
    setTabIndexForMediaElements(); // Set tabindex for media elements
    const mediaElements = getMediaElements();
    mediaElements.forEach((element, index) => {
        element.addEventListener('click', handleMediaClick(element, index));
    });
}

// Initialize media listeners on load
initializeMediaListeners();

// Add overlay click event listener
overlay.addEventListener('click', (event) => {
    if (fullscreenElement && !fullscreenElement.contains(event.target) && !wasDragging) {
        if (fullscreenElement.tagName === 'VIDEO') {
            fullscreenElement.pause();
            fullscreenElement.currentTime = 0;
        }
        toggleFullscreen(); // Exit fullscreen when clicking outside the media
    }
});

// Keydown event handler for exiting fullscreen with ESC, play/pause, and rewind/fast-forward
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && fullscreenElement) {
        if (fullscreenElement.tagName === 'VIDEO') {
            fullscreenElement.pause();
            fullscreenElement.currentTime = 0;
        }
        toggleFullscreen(); // Exit fullscreen
    } else if (event.key === 'ArrowRight' && fullscreenElement) {
        if (fullscreenElement.tagName === 'VIDEO') {
            fullscreenElement.pause();
            fullscreenElement.currentTime = 0;
        }
        navigateMedia(1); // Right arrow for next media
    } else if (event.key === 'ArrowLeft' && fullscreenElement) {
        if (fullscreenElement.tagName === 'VIDEO') {
            fullscreenElement.pause();
            fullscreenElement.currentTime = 0;
        }
        navigateMedia(-1); // Left arrow for previous media
	} else if (event.key === 'End' && fullscreenElement) { // Reset zoom and potision
        navigateMedia(1); // Right arrow for next media
		navigateMedia(-1); // Left arrow for previous media
    } else if (event.key === 'Delete' && fullscreenElement && fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.currentTime = Math.max(0, fullscreenElement.currentTime - 10); // Rewind 10 seconds
    } else if (event.key === 'PageDown' && fullscreenElement && fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.currentTime = Math.min(fullscreenElement.duration, fullscreenElement.currentTime + 10); // Fast-forward 10 seconds
    } else if (event.key === 'Insert' && fullscreenElement && fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.currentTime = Math.max(0, fullscreenElement.currentTime - 5); // Rewind 5 seconds
    } else if (event.key === 'PageUp' && fullscreenElement && fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.currentTime = Math.min(fullscreenElement.duration, fullscreenElement.currentTime + 5); // Fast-forward 5 seconds
    } else if (event.code === 'Space' && fullscreenElement && fullscreenElement.tagName === 'VIDEO') {
        if (fullscreenElement.paused) {
            fullscreenElement.play();
        } else {
            fullscreenElement.pause();
        }
    }
});

// Function to calculate zoom speed based on current scale
function calculateDelta(scale) {
    const baseDelta = 0.1; // Base zoom speed
    const scaleFactor = 0.3; // Factor by which the zoom speed increases
    return baseDelta * Math.pow(1 + scaleFactor, scale - 1); // Exponential increase in zoom speed
}

// Handle zoom functionality
function handleZoom(event) {
    event.preventDefault(); // Prevent default scroll behavior

    // Check if the mouse is not over the tagsManagementContainer
    if (!tagsManagementContainer.contains(event.target)) {
        const zoomSpeed = calculateDelta(scale); // Calculate zoom speed based on current scale
        const delta = event.deltaY < 0 ? zoomSpeed : -zoomSpeed; // Zoom in if scroll up, out if scroll down

        scale += delta;
        scale = Math.max(0.1, Math.min(scale, 10)); // Limit scale between 0.1 and 10

        fullscreenElement.style.transform = `scale(${scale})`; // Apply scale transformation
    }
}

document.addEventListener('wheel', handleZoom);

// Recenter media on window resize
window.addEventListener('resize', () => {
    if (fullscreenElement) {
        centerElement(fullscreenElement);
    }
});

// Arrow navigation event handlers
nextArrow.addEventListener('click', () => {
    if (fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.pause();
        fullscreenElement.currentTime = 0;
    }
    navigateMedia(1); // Go to next media
});

prevArrow.addEventListener('click', () => {
    if (fullscreenElement.tagName === 'VIDEO') {
        fullscreenElement.pause();
        fullscreenElement.currentTime = 0;
    }
    navigateMedia(-1); // Go to previous media
});

function navigateMedia(direction) {
    const mediaElements = getMediaElements(); // Get updated media elements
    currentIndex = (currentIndex + direction + mediaElements.length) % mediaElements.length;
    toggleFullscreen(); // Exit current fullscreen
    const newElement = mediaElements[currentIndex];
    toggleFullscreen(newElement); // Enter fullscreen for the new media
    // Do not auto-play the video after switching
}

// Variables to handle dragging
let startX, startY, initialLeft, initialTop;
let isDragging = false;
let isDragOnMedia = false; // New variable to track if drag started on media

function onMouseDown(event) {
    if (fullscreenElement && !tagsManagementContainer.contains(event.target)) {
        isDragging = true;
        wasDragging = false; // Reset wasDragging when starting to drag
        startX = event.clientX;
        startY = event.clientY;
        initialLeft = parseFloat(window.getComputedStyle(fullscreenElement).left);
        initialTop = parseFloat(window.getComputedStyle(fullscreenElement).top);
        // Check if mouse is down on the media element
        if (fullscreenElement.contains(event.target)) {
            isDragOnMedia = true;
        }
        event.preventDefault(); // Prevent default behavior
    }
}

function onMouseMove(event) {
    if (isDragging && fullscreenElement) {
        const dx = event.clientX - startX;
        const dy = event.clientY - startY;
        fullscreenElement.style.left = `${initialLeft + dx}px`;
        fullscreenElement.style.top = `${initialTop + dy}px`;
        wasDragging = true; // Set wasDragging to true during dragging
    }
}


let focusInterval = null;

// Function to ensure focus is set to the search bar
function ensureFocusOnSearchBar() {
    // Check if the search bar does not currently have focus
    if (document.activeElement !== searchBar) {
        searchBar.focus();
    }
}

// Start a periodic check to ensure the search bar gets focus
function startFocusInterval() {
    if (!focusInterval) {
        focusInterval = setInterval(ensureFocusOnSearchBar, 100); // Check every 100ms
    }
}

// Stop the periodic check
function stopFocusInterval() {
    if (focusInterval) {
        clearInterval(focusInterval);
        focusInterval = null;
    }
}

// Function to handle mouse up event
function onMouseUp() {
    if (fullscreenElement) {
        if (wasDragging && isDragOnMedia && fullscreenElement.tagName === 'VIDEO') {
            if (fullscreenElement.paused) {
                fullscreenElement.play();
            } else {
                setTimeout(() => fullscreenElement.play(), 0.001); // Slight pause adjustment
            }
        }
        isDragging = false;
        isDragOnMedia = false;
    }
    
    // Ensure the focus interval is started
    startFocusInterval();
}

// Add event listeners
document.addEventListener('mousedown', onMouseDown);
document.addEventListener('mousemove', onMouseMove);
document.addEventListener('mouseup', onMouseUp);

// Optionally stop the focus interval when it's not needed
window.addEventListener('blur', stopFocusInterval);
window.addEventListener('focus', startFocusInterval);

document.addEventListener('DOMContentLoaded', function() {
    const confirmButton = document.getElementById('confirmButton');
	const searchButton = document.getElementById('searchButton');
	
	// Select all tag items
	        const tagItems = document.querySelectorAll('.tag-item');
	        // Add click event listener to each tag item
	        tagItems.forEach(tag => {
	            tag.addEventListener('click', function() {
	                // Get the text of the clicked tag
	                const tagText = this.textContent;
	                // Append the tag text followed by a space to the current value of the search bar
	                searchBar.value += tagText + ' ';
	            });
	        });

			buttons.forEach(button => {
			    button.addEventListener('click', function() {
			        if (this.classList.contains('hierarchy-button')) {
			            
			            // Toggle the selection state for the clicked hierarchical button
			            if (this.classList.contains('selected')) {
			                this.classList.remove('selected');
			            } else {
			                this.classList.add('selected');
			            }

			            // Toggle visibility of child buttons, if any
			            const childButtons = this.nextElementSibling;
			            if (childButtons && childButtons.classList.contains('child-buttons')) {
			                childButtons.style.display = 
			                    childButtons.style.display === 'block' ? 'none' : 'block';
			            }
			        } else {
			            // Toggle the selection state for tag buttons
			            this.classList.toggle('selected');
			        }
			    });
			});

			
			
			/**
			 * Sends a POST request with JSON data.
			 *
			 * @param {string} url - The URL to send the POST request to.
			 * @param {object} data - The data to be sent in the request body.
			 * @returns {Promise<object>} - A promise that resolves with the response data.
			 */
			async function postJson(url, data) {
			  try {
			    const response = await fetch(url, {
			      method: 'POST',
			      headers: {
			        'Content-Type': 'application/json',
			      },
			      body: JSON.stringify(data),
			    });
				
			    if (!response.ok) {
			      throw new Error(`HTTP error! Status: ${response.status}`);
			    }

			    const result = await response.json();
			    return result;
			  } catch (error) {
			    console.error('Error:', error);
			    throw error;
			  }
			}
			
			async function postSearchJson(url, data) {
			  try {
			    const response = await fetch(url, {
			      method: 'POST',
			      headers: {
			        'Content-Type': 'application/json',
			      },
			      body: JSON.stringify(data),
			    });

			    if (!response.ok) {
			      throw new Error(`HTTP error! Status: ${response.status}`);
			    }
			    window.location.href = '/';
			    
			  } catch (error) {
			    console.error('Error:', error);
			  }
			}
			
			
			searchButton.addEventListener('click', function() {
			    // Get the value from the input field with class 'search-bar'
			    const elements = document.querySelectorAll('.search-bar');
			    const printedTags = elements[0].value.trim().split(' ');

			    // Prepare the data to be sent
			    const data = {
			        selectedTags: printedTags,
			    };

			    // Send the data via a POST request
			    const url = '/sendSearchTags';
			    postSearchJson(url, data);
			});
	
			
    confirmButton.addEventListener('click', function() {
        buttons.forEach(button => {
            // If the button is selected, confirm it; otherwise, remove confirmation
            if (button.classList.contains('selected')) {
                button.classList.add('confirmed');
            } else {
                button.classList.remove('confirmed');
            }
        });
		
		// Allow users to unselect confirmed buttons after confirming
        buttons.forEach(button => {
            button.addEventListener('click', function () {
                if (this.classList.contains('confirmed') && this.classList.contains('tag-button')) {
                    this.classList.remove('confirmed');
                }
            });
        });

		const url = '/sendTags';
		const elements = document.querySelectorAll('.hierarchy-button.selected, .tag-button.selected');
		const tags = Array.from(elements).map(element => element.textContent.trim());
		const data = {
		  selectedTags: tags,
		  currentFileIndex: currentIndex,
		  fileLocation: mediaFiles[currentIndex],
		};
		postJson(url, data);
    });

	
	searchBar.addEventListener('keydown', function(event) {
	    if (event.key === 'Enter') {
	        event.preventDefault();
	        const url = '/sendSearchTags';
	        const elements = document.querySelectorAll('.search-bar');
	        const printedTags = elements[0].value.trim().split(' ');

	        const data = {
	            selectedTags: printedTags,
	        };

	        postSearchJson(url, data);
	    }
	});
	
	
    // Event listener (former 'M' key press)
    document.addEventListener('keydown', function(event) {
        if (fullscreenElement && (event.key === 'ArrowDown' || event.key === 'ArrowUp')) {
            // Toggle visibility of the side-container
            if (tagsManagementContainer.style.display === 'none' || tagsManagementContainer.style.display === '') {
                tagsManagementContainer.style.display = 'block';
				prevArrow.style.display = 'none'; // Hide arrows
				nextArrow.style.display = 'none';
            } else {
                tagsManagementContainer.style.display = 'none';
				prevArrow.style.display = 'block';
				nextArrow.style.display = 'block';
            }
        }
    });

});