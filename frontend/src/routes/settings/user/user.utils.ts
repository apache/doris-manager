import passwordGenerator from 'password-generator';

function isStrongEnough(password: string) {
    return /^(?![a-zA-Z]+$)(?![A-Z\d]+$)(?![A-Z_]+$)(?![a-z\d]+$)(?![a-z_]+$)(?![\d_]+$)[a-zA-Z\d_]{6,12}$/.test(
        password,
    );
}

export function generatePassword() {
    let password = passwordGenerator(12, false, /[a-zA-Z\d_]/);
    while (!isStrongEnough(password)) {
        password = passwordGenerator(12, false, /[a-zA-Z\d_]/);
    }
    return password;
}

export function copyText(text: string) {
    const textArea = document.createElement('textarea');
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.value = text;
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
}
