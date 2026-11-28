(() => {
  const nav = document.getElementById("nav");
  const toTop = document.getElementById("toTop");

  const onScroll = () => {
    const y = window.scrollY;
    nav.classList.toggle("scrolled", y > 8);
    toTop.classList.toggle("show", y > 640);
  };
  onScroll();
  window.addEventListener("scroll", onScroll, { passive: true });

  toTop.addEventListener("click", () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  });

  const reveals = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) continue;
          const el = entry.target;
          const delay = el.dataset.delay || "0";
          el.style.setProperty("--d", `${delay}ms`);
          el.classList.add("in");
          io.unobserve(el);
        }
      },
      { rootMargin: "0px 0px -8% 0px", threshold: 0.12 }
    );
    reveals.forEach((el) => io.observe(el));
  } else {
    reveals.forEach((el) => el.classList.add("in"));
  }

  // 用 GitHub 上的最新 release 校正版本号与下载链接；接口不可用时保留静态内容。
  const API = "https://api.github.com/repos/Youzix-Star/yizhong-bang/releases/latest";
  fetch(API, { headers: { Accept: "application/vnd.github+json" } })
    .then((r) => (r.ok ? r.json() : null))
    .then((data) => {
      if (!data || !data.tag_name) return;
      const version = String(data.tag_name).replace(/^v/i, "");
      document.querySelectorAll("[data-version]").forEach((el) => {
        el.textContent = version;
      });
      const primary = document.querySelector(".dl-primary");
      if (primary && data.html_url) primary.href = data.html_url;
    })
    .catch(() => {});
})();
