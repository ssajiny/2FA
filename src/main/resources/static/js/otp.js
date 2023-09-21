function updateCurrentTime() {
    let currentTimeElement = document.getElementById('currentTime');
    let checkCodeElement = document.getElementById('checkCode');
    let secretKeyElement = document.getElementById('secretKey');
    let divElement = document.getElementById("progressBar");

    let now = new Date();
    let currentTimeString = now.toLocaleTimeString();
    let minutes = 30 - ((Math.floor(now.getTime() / 1000)) % 30);
    divElement.style.width = (minutes/30*100) + "%";

    if(minutes == 1 || minutes == 30) {
        fetchCheckCode(secretKeyElement.value)
            .then(function(secretKey) {
                secretKey = secretKey.toString().padStart(6, '0');
                checkCodeElement.innerText = secretKey;
            });
    }

    if (minutes <= 5) {
        divElement.style.backgroundColor = "red";
    } else {
        divElement.style.backgroundColor = "";
    }
    currentTimeElement.innerText = '현재 시간: ' + currentTimeString;

}

function fetchCheckCode(key) {
    let url = '/otp/fetchCheck';
    let data = {
        secretKey: key,
    };

    let requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    };

    return fetch(url, requestOptions)
        .then(function (response) {
            if (!response.ok) {
                throw new Error('HTTP 오류, 상태 코드: ' + response.status);
            }
            return response.json();
        })
        .then(function (responseData) {
            return responseData;
        })
        .catch(function (error) {
            console.error('에러 발생:', error);
        });
}

window.onload =  function() {
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
};