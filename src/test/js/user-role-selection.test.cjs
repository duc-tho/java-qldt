const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const source = readFileSync(
  path.resolve(__dirname, "../../main/resources/script/user-role-selection.js"),
  "utf8",
);

function roleSelection(checkedRoles = []) {
  let factory;
  const Alpine = { data: (_, component) => { factory = component; } };
  vm.runInNewContext(source, { Alpine, window: { Alpine } });

  const boxes = ["ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT"].map((name) => ({
    dataset: { roleName: name },
    checked: checkedRoles.includes(name),
    disabled: false,
  }));
  const component = factory();
  component.$root = { querySelectorAll: () => boxes };
  component.$el = component.$root;
  return { component, boxes: Object.fromEntries(boxes.map((box) => [box.dataset.roleName, box])) };
}

test("initial lecturer state allows admin pairing or switching to student", () => {
  const { component, boxes } = roleSelection(["LECTURER"]);
  component.init?.();

  assert.equal(boxes.ADMIN.disabled, false);
  assert.equal(boxes.HEAD_OF_FACULTY.disabled, true);
  assert.equal(boxes.STUDENT.disabled, false);
});

test("allowed pair disables every remaining role and unchecking re-enables valid options", () => {
  const { component, boxes } = roleSelection(["ADMIN"]);
  component.init?.();
  boxes.LECTURER.checked = true;
  component.select(boxes.LECTURER);

  assert.equal(boxes.HEAD_OF_FACULTY.disabled, true);
  assert.equal(boxes.STUDENT.disabled, true);

  boxes.LECTURER.checked = false;
  component.select(boxes.LECTURER);

  assert.equal(boxes.HEAD_OF_FACULTY.disabled, false);
  assert.equal(boxes.LECTURER.disabled, false);
  assert.equal(boxes.STUDENT.disabled, true);
});

test("a selected student disables all other roles", () => {
  const { component, boxes } = roleSelection(["STUDENT"]);
  component.init?.();

  assert.equal(boxes.ADMIN.disabled, true);
  assert.equal(boxes.HEAD_OF_FACULTY.disabled, true);
  assert.equal(boxes.LECTURER.disabled, true);
});

test("unchecking the only selected admin enables all four roles", () => {
  const { component, boxes } = roleSelection(["ADMIN"]);
  component.init?.();
  boxes.ADMIN.checked = false;
  component.select(boxes.ADMIN);

  assert.equal(boxes.ADMIN.disabled, false);
  assert.equal(boxes.HEAD_OF_FACULTY.disabled, false);
  assert.equal(boxes.LECTURER.disabled, false);
  assert.equal(boxes.STUDENT.disabled, false);
});

test("unchecking admin from a pair enables switching the remaining role to student", () => {
  const { component, boxes } = roleSelection(["ADMIN", "LECTURER"]);
  component.init?.();
  assert.equal(boxes.STUDENT.disabled, true);

  component.$el = { querySelectorAll: () => [] };
  boxes.ADMIN.checked = false;
  component.select(boxes.ADMIN);
  assert.equal(boxes.STUDENT.disabled, false);

  boxes.STUDENT.checked = true;
  component.select(boxes.STUDENT);
  assert.equal(boxes.LECTURER.checked, false);
  assert.equal(boxes.ADMIN.disabled, true);
  assert.equal(boxes.HEAD_OF_FACULTY.disabled, true);
  assert.equal(boxes.LECTURER.disabled, true);
});

test("switching from head of faculty to student keeps a single selected role", () => {
  const { component, boxes } = roleSelection(["HEAD_OF_FACULTY"]);
  component.init?.();
  assert.equal(boxes.STUDENT.disabled, false);

  boxes.STUDENT.checked = true;
  component.select(boxes.STUDENT);
  assert.equal(boxes.HEAD_OF_FACULTY.checked, false);
});
