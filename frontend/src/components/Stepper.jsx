export function Stepper({ stepIndex, canGoOrder, canGoPayment, canGoReceipt, onGoOrder, onGoPayment, onGoReceipt }) {
  return (
    <div className="stepper">
      <div className={`step ${stepIndex >= 0 ? 'active' : ''} ${canGoOrder ? 'clickable' : ''}`} onClick={canGoOrder ? onGoOrder : undefined}>
        <div className={`stepDot ${stepIndex >= 0 ? 'active' : ''}`}>1</div>
        <div className="stepLabel">Pesanan</div>
      </div>
      <div className={`stepLine ${stepIndex >= 1 ? 'active' : ''}`} />
      <div className={`step ${stepIndex >= 1 ? 'active' : ''} ${canGoPayment ? 'clickable' : ''}`} onClick={canGoPayment ? onGoPayment : undefined}>
        <div className={`stepDot ${stepIndex >= 1 ? 'active' : ''}`}>2</div>
        <div className="stepLabel">Pembayaran</div>
      </div>
      <div className={`stepLine ${stepIndex >= 2 ? 'active' : ''}`} />
      <div className={`step ${stepIndex >= 2 ? 'active' : ''} ${canGoReceipt ? 'clickable' : ''}`} onClick={canGoReceipt ? onGoReceipt : undefined}>
        <div className={`stepDot ${stepIndex >= 2 ? 'active' : ''}`}>3</div>
        <div className="stepLabel">Struk</div>
      </div>
    </div>
  )
}

