it("1 + 1 equals 2", async () => {
  expect(1 + 1).toEqual(2);
});

it("2 + 1 equals 3", async () => {
  expect(2 + 1).toEqual(3);
});

it("sum associative", async () => {
  expect(2 + 1 + 3).toEqual(2 + (1 + 3));
});
