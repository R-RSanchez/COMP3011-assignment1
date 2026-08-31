const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("status");

let mediaRecorder;
let audioChunks = [];

startButton.addEventListener("click", async () => {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];

	mediaRecorder.addEventListener("dataavailable", event => {
	    audioChunks.push(event.data);
	});
	
	mediaRecorder.addEventListener("stop", () => {

	    const audioBlob = new Blob(audioChunks, {
	        type: mediaRecorder.mimeType
	    });

	    console.log("Audio size:", audioBlob.size);
	    console.log("Audio type:", audioBlob.type);

	    stream.getTracks().forEach(track => track.stop());
	});
	
    mediaRecorder.start();

    statusText.textContent = "Recording...";
    startButton.disabled = true;
    stopButton.disabled = false;
});

stopButton.addEventListener("click", () => {
    mediaRecorder.stop();

    statusText.textContent = "Ready";
    startButton.disabled = false;
    stopButton.disabled = true;
}); 