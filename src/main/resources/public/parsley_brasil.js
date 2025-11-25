// ---------------------
// Funções utilitárias
// ---------------------
function onlyDigits(v) {
    return (v || "").replace(/\D+/g, "");
}

function repeated(v) {
    return /^(\d)\1+$/.test(v);
}

// ---------------------
// Validação CPF
// ---------------------
function validateCPF(value) {
    let cpf = onlyDigits(value);
    if (cpf.length !== 11) return false;
    if (repeated(cpf)) return false;

    let sum = 0, rest;

    for (let i = 1; i <= 9; i++)
        sum += parseInt(cpf[i - 1]) * (11 - i);

    rest = (sum * 10) % 11;
    if (rest === 10 || rest === 11) rest = 0;
    if (rest !== parseInt(cpf[9])) return false;

    sum = 0;
    for (let i = 1; i <= 10; i++)
        sum += parseInt(cpf[i - 1]) * (12 - i);

    rest = (sum * 10) % 11;
    if (rest === 10 || rest === 11) rest = 0;

    return rest === parseInt(cpf[10]);
}

// ---------------------
// Validação CNPJ
// ---------------------
function validateCNPJ(value) {
    let cnpj = onlyDigits(value);
    if (cnpj.length !== 14) return false;
    if (repeated(cnpj)) return false;

    let size = cnpj.length - 2;
    let numbers = cnpj.substring(0, size);
    let digits = cnpj.substring(size);

    let sum = 0;
    let pos = size - 7;

    for (let i = size; i >= 1; i--) {
        sum += parseInt(numbers[size - i]) * pos--;
        if (pos < 2) pos = 9;
    }

    let result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    if (result != digits[0]) return false;

    size++;
    numbers = cnpj.substring(0, size);
    sum = 0;
    pos = size - 7;

    for (let i = size; i >= 1; i--) {
        sum += parseInt(numbers[size - i]) * pos--;
        if (pos < 2) pos = 9;
    }

    result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    return result == digits[1];
}

// ---------------------
// Parsley Validators
// ---------------------
window.Parsley.addValidator("cpf", {
    validateString: value => validateCPF(value),
    messages: {
        "pt-br": "CPF inválido.",
        "en": "Invalid CPF."
    }
});

window.Parsley.addValidator("cnpj", {
    validateString: value => validateCNPJ(value),
    messages: {
        "pt-br": "CNPJ inválido.",
        "en": "Invalid CNPJ."
    }
});

window.Parsley.addValidator("cpfcnpj", {
    validateString: value => {
        const d = onlyDigits(value);
        if (d.length === 11) return validateCPF(d);
        if (d.length === 14) return validateCNPJ(d);
        return false;
    },
    messages: {
        "pt-br": "CPF ou CNPJ inválido.",
        "en": "Invalid CPF or CNPJ."
    }
});
