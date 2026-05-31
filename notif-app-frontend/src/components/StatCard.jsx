import React from "react";
import { Card } from "primereact/card";
import { Button } from "primereact/button";
import { ProgressBar } from "primereact/progressbar";

const StatsCard = ({ title, value, icon, color, onClick, loading, percentage }) => {
  return (
    <Card className={`stats-card stats-card--${color}`}>
      <div className="stats-card__body">
        <div className="stats-card__left">
          <span className="stats-card__title">{title}</span>
          <span className="stats-card__value">
            {loading ? "—" : (value?.toLocaleString() ?? 0)}
          </span>
          {percentage != null && (
            <div className="stats-card__progress">
              <ProgressBar
                value={percentage}
                showValue={false}
                style={{ height: "4px", background: "#ffffff18" }}
                color={`var(--color-${color})`}
              />
              <span className="stats-card__pct">{percentage}%</span>
            </div>
          )}
        </div>
        <div className="stats-card__right">
          <div className="stats-card__icon-wrap">
            <i className={`pi ${icon}`} />
          </div>
        </div>
      </div>
      {onClick && (
        <Button
          label="View All"
          icon="pi pi-arrow-right"
          iconPos="right"
          className={`stats-card__btn p-button-text p-button-sm`}
          onClick={onClick}
        />
      )}
    </Card>
  );
};

export default StatsCard;