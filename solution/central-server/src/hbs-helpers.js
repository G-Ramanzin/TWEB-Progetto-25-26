/**
 * @fileoverview Handlebars helpers used by the frontend views.
 * Registered once in server.js when the view engine is configured.
 */

module.exports = {
    /**
     * Locale-formatted number, 'N/A' when missing.
     * @param {number} num
     * @returns {string}
     */
    formatNumber(num) {
        if (num === null || num === undefined) return 'N/A';
        return Number(num).toLocaleString();
    },

    /**
     * Truncates a string adding an ellipsis.
     * @param {string} str
     * @param {number} len
     * @returns {string}
     */
    truncate(str, len) {
        if (!str) return '';
        return str.length <= len ? str : str.substring(0, len) + '…';
    },

    /**
     * Strict equality, usable as subexpression: {{#if (eq a b)}}.
     */
    eq(a, b) {
        return a === b;
    },

    /**
     * Basic arithmetic for pagination links: {{math page '+' 1}}.
     * @returns {number}
     */
    math(a, op, b) {
        a = Number(a); b = Number(b);
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            default: return a;
        }
    },

    /**
     * Extracts one property from every object of an array; used to
     * feed labels/values to the Chart.js data attributes.
     * @param {Object[]} arr
     * @param {string} key
     * @returns {Array}
     */
    pluck(arr, key) {
        return Array.isArray(arr) ? arr.map(o => o[key]) : [];
    },

    /**
     * JSON stringify used to embed data into data-* attributes
     * for the Chart.js charts. Escapes '<' to avoid closing the tag.
     * @param {*} context
     * @returns {string}
     */
    json(context) {
        return JSON.stringify(context === undefined ? null : context)
            .replace(/</g, '\\u003c');
    },

    /**
     * Splits a comma separated string into an array, used to turn the
     * genres CSV column into clickable badges.
     * @param {string} str
     * @returns {string[]}
     */
    split(str) {
        // The dataset stores lists as Python-style strings like
        // "['Action', 'Adventure']": brackets and quotes are stripped.
        return str ? String(str).replace(/[\[\]'"]/g, '').split(',') : [];
    },

    /**
     * Renders a Python-style list string as readable text:
     * "['A', 'B']" -> "A, B". Empty lists become an empty string.
     * @param {string} str
     * @returns {string}
     */
    cleanList(str) {
        if (!str) return '';
        return String(str).replace(/[\[\]'"]/g, '').split(',')
            .map(t => t.trim()).filter(Boolean).join(', ');
    },

    /**
     * Trims a string (paired with split for the genre badges).
     * @param {string} str
     * @returns {string}
     */
    trim(str) {
        return str ? String(str).trim() : '';
    },

    /**
     * Block helper with a comparison operator:
     * {{#ifCond page '<' totalPages}} ... {{/ifCond}}
     */
    ifCond(v1, operator, v2, options) {
        let ok;
        switch (operator) {
            case '==': ok = (v1 == v2); break;
            case '===': ok = (v1 === v2); break;
            case '!=': ok = (v1 != v2); break;
            case '>': ok = (v1 > v2); break;
            case '<': ok = (v1 < v2); break;
            case '>=': ok = (v1 >= v2); break;
            case '<=': ok = (v1 <= v2); break;
            default: ok = false;
        }
        return ok ? options.fn(this) : options.inverse(this);
    }
};
